package io.github.ragportfolio.application;

import io.github.ragportfolio.application.port.AccessPolicyPort;
import io.github.ragportfolio.application.port.DocumentCatalogPort;
import io.github.ragportfolio.application.port.GenerateAnswerPort;
import io.github.ragportfolio.application.port.GeneratedAnswer;
import io.github.ragportfolio.application.port.ObservationPort;
import io.github.ragportfolio.application.port.RetrieveEvidencePort;
import io.github.ragportfolio.domain.Evidence;
import java.util.List;
import java.util.Objects;

public final class QuestionService implements AskQuestionUseCase {
    private final RetrieveEvidencePort retrieval;
    private final GenerateAnswerPort generation;
    private final DocumentCatalogPort catalog;
    private final AccessPolicyPort access;
    private final ObservationPort observation;

    public QuestionService(RetrieveEvidencePort retrieval, GenerateAnswerPort generation,
            DocumentCatalogPort catalog, AccessPolicyPort access, ObservationPort observation) {
        this.retrieval = Objects.requireNonNull(retrieval);
        this.generation = Objects.requireNonNull(generation);
        this.catalog = Objects.requireNonNull(catalog);
        this.access = Objects.requireNonNull(access);
        this.observation = Objects.requireNonNull(observation);
    }

    @Override
    public AnswerResult ask(AskQuestionCommand command) {
        Objects.requireNonNull(command, "command");
        long started = System.nanoTime();
        var outcome = ObservationPort.Outcome.ERROR;
        int evidenceCount = 0;
        try {
            var scope = access.scopeFor(command.actor());
            if (!scope.tenantId().equals(command.actor().tenantId())) {
                throw new IllegalStateException("Access policy returned a different tenant");
            }
            List<Evidence> authorized = scope.documentIds().isEmpty() ? List.of()
                    : retrieval.retrieve(command.question(), command.topK(), scope).stream()
                    .filter(e -> scope.tenantId().equals(e.tenantId()) && scope.documentIds().contains(e.documentId()))
                    .filter(e -> catalog.findCurrent(command.actor().tenantId(), e.documentId())
                            .filter(d -> d.isCurrent(e, command.actor()) && access.canRead(command.actor(), d)).isPresent())
                    .limit(command.topK()).toList();
            evidenceCount = authorized.size();
            if (authorized.isEmpty()) {
                outcome = ObservationPort.Outcome.NO_CONTEXT;
                return new AnswerResult(AnswerResult.Outcome.NO_CONTEXT, "No authorized current evidence.", List.of(), "NONE");
            }
            GeneratedAnswer generated;
            try {
                generated = generation.generate(command.question(), authorized);
            } catch (AdapterNotImplementedException exception) {
                throw exception;
            } catch (RuntimeException exception) {
                throw new GenerationFailedException(exception);
            }
            var citations = authorized.stream().map(e -> new Citation(e.documentId(), e.version(), e.locator())).toList();
            outcome = ObservationPort.Outcome.ANSWER;
            var result = generated.mode() == GeneratedAnswer.Mode.MOCK
                    ? AnswerResult.Outcome.MOCK_ANSWER : AnswerResult.Outcome.GENERATED_ANSWER;
            return new AnswerResult(result, generated.text(), citations, generated.mode().name());
        } finally {
            try {
                observation.completed(outcome, System.nanoTime() - started, evidenceCount);
            } catch (RuntimeException ignored) {
                // Observability must not replace the use case's result or original failure.
            }
        }
    }
}
