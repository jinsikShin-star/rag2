package io.github.ragportfolio.application;

import io.github.ragportfolio.application.port.AccessPolicyPort;
import io.github.ragportfolio.application.port.DocumentCatalogPort;
import io.github.ragportfolio.application.port.GenerateAnswerPort;
import io.github.ragportfolio.application.port.GeneratedAnswer;
import io.github.ragportfolio.application.port.ObservationPort;
import io.github.ragportfolio.application.port.RetrieveEvidencePort;
import io.github.ragportfolio.application.port.SearchScope;
import io.github.ragportfolio.domain.Actor;
import io.github.ragportfolio.domain.Document;
import io.github.ragportfolio.domain.Evidence;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuestionServiceTest {
    private static final Actor ACTOR = new Actor("tenant", "user");
    private static final AskQuestionCommand COMMAND = new AskQuestionCommand(ACTOR, "fixture question", 5);
    private static final Evidence CURRENT = evidence("current", "tenant", 1);
    private static final GenerateAnswerPort MOCK = (question, evidence) -> new GeneratedAnswer("[MOCK] answer", GeneratedAnswer.Mode.MOCK);
    private static final ObservationPort SILENT = (outcome, elapsed, count) -> { };

    @Test void passesScopeAndOnlyCurrentAuthorizedEvidenceToGeneration() {
        var received = new AtomicReference<List<Evidence>>();
        var observed = new AtomicReference<ObservationPort.Outcome>();
        var policy = policy(Set.of("current", "old", "deleted", "revoked", "unknown", "cross"));
        var documents = Map.of(
                "current", document("current", 1, Document.Status.ACTIVE),
                "old", document("old", 2, Document.Status.ACTIVE),
                "deleted", document("deleted", 1, Document.Status.DELETED),
                "revoked", document("revoked", 1, Document.Status.ACTIVE),
                "cross", new Document("cross", "other", 1, Document.Status.ACTIVE));
        RetrieveEvidencePort retrieval = (question, topK, scope) -> {
            assertEquals("tenant", scope.tenantId());
            assertEquals(policy.scopeFor(ACTOR), scope);
            assertEquals(5, topK);
            return List.of(evidence("old", "tenant", 1), evidence("deleted", "tenant", 1),
                    evidence("revoked", "tenant", 1), evidence("unknown", "tenant", 1),
                    evidence("cross", "other", 1), evidence("outside-scope", "tenant", 1), CURRENT);
        };
        GenerateAnswerPort generation = (question, evidence) -> {
            received.set(evidence);
            assertThrows(UnsupportedOperationException.class, () -> evidence.add(CURRENT));
            return MOCK.generate(question, evidence);
        };
        var service = new QuestionService(retrieval, generation,
                (tenant, id) -> Optional.ofNullable(documents.get(id)), policy,
                (outcome, elapsed, count) -> { observed.set(outcome); assertEquals(1, count); assertTrue(elapsed >= 0); });
        var result = service.ask(COMMAND);
        assertEquals(List.of(CURRENT), received.get());
        assertEquals(AnswerResult.Outcome.MOCK_ANSWER, result.outcome());
        assertEquals("MOCK", result.generationMode());
        assertEquals(List.of(new Citation("current", 1, "fixture:1")), result.citations());
        assertThrows(UnsupportedOperationException.class, () -> result.citations().clear());
        assertEquals(ObservationPort.Outcome.ANSWER, observed.get());
    }

    @Test void emptyScopeAvoidsRetrievalAndGeneration() {
        var service = new QuestionService((q, k, s) -> { fail("must not retrieve"); return List.of(); },
                (q, e) -> { fail("must not generate"); return null; }, emptyCatalog(), policy(Set.of()), SILENT);
        var result = service.ask(COMMAND);
        assertEquals(AnswerResult.Outcome.NO_CONTEXT, result.outcome());
        assertEquals("NONE", result.generationMode());
        assertTrue(result.citations().isEmpty());
    }

    @Test void filteredOrMissingEvidenceAvoidsGeneration() {
        var service = new QuestionService((q, k, s) -> List.of(CURRENT),
                (q, e) -> { fail("must not generate"); return null; }, emptyCatalog(), policy(Set.of("current")), SILENT);
        assertEquals(AnswerResult.Outcome.NO_CONTEXT, service.ask(COMMAND).outcome());
    }

    @Test void emptyRetrievalAvoidsGeneration() {
        var service = new QuestionService((q, k, s) -> List.of(),
                (q, e) -> { fail("must not generate"); return null; }, emptyCatalog(), policy(Set.of("current")), SILENT);
        assertEquals(AnswerResult.Outcome.NO_CONTEXT, service.ask(COMMAND).outcome());
    }

    @Test void wrongTenantPolicyFailsClosedBeforeRetrieval() {
        AccessPolicyPort wrong = new AccessPolicyPort() {
            @Override public SearchScope scopeFor(Actor actor) { return new SearchScope("other", Set.of("current")); }
            @Override public boolean canRead(Actor actor, Document document) { return true; }
        };
        var service = new QuestionService((q, k, s) -> { fail("must not retrieve"); return List.of(); }, MOCK,
                emptyCatalog(), wrong, SILENT);
        assertThrows(IllegalStateException.class, () -> service.ask(COMMAND));
    }

    @Test void mismatchingCatalogIdentityCannotAuthorizeEvidence() {
        var service = new QuestionService((q, k, s) -> List.of(CURRENT),
                (q, e) -> { fail("must not generate"); return null; },
                (tenant, id) -> Optional.of(document("different", 1, Document.Status.ACTIVE)),
                policy(Set.of("current")), SILENT);
        assertEquals(AnswerResult.Outcome.NO_CONTEXT, service.ask(COMMAND).outcome());
    }

    @Test void limitsAuthorizedEvidenceToRequestedTopK() {
        var received = new AtomicReference<List<Evidence>>();
        var service = service((q, e) -> { received.set(e); return MOCK.generate(q, e); }, SILENT,
                (q, k, scope) -> List.of(CURRENT, CURRENT));
        service.ask(new AskQuestionCommand(ACTOR, "question", 1));
        assertEquals(1, received.get().size());
    }

    @Test void generationFailureKeepsCauseAndObservationCannotMaskIt() {
        var failure = new IllegalStateException("provider fixture failure");
        var observed = new AtomicReference<ObservationPort.Outcome>();
        var service = service((q, e) -> { throw failure; }, (outcome, elapsed, count) -> {
            observed.set(outcome); throw new IllegalStateException("observer fixture failure");
        }, (q, k, s) -> List.of(CURRENT));
        var exception = assertThrows(GenerationFailedException.class, () -> service.ask(COMMAND));
        assertSame(failure, exception.getCause());
        assertEquals(ObservationPort.Outcome.ERROR, observed.get());
    }

    @Test void unimplementedAdapterRemainsAnExplicitFailure() {
        var failure = new AdapterNotImplementedException("fixture");
        var service = service((q, e) -> { throw failure; }, SILENT, (q, k, s) -> List.of(CURRENT));
        assertSame(failure, assertThrows(AdapterNotImplementedException.class, () -> service.ask(COMMAND)));
    }

    @Test void observationFailureDoesNotChangeSuccessfulResult() {
        var service = service(MOCK, (o, e, c) -> { throw new IllegalStateException("observer"); },
                (q, k, s) -> List.of(CURRENT));
        assertEquals(AnswerResult.Outcome.MOCK_ANSWER, service.ask(COMMAND).outcome());
    }

    @Test void futureExternalPortResultIsNeverLabelledAsMock() {
        var service = service((q, e) -> new GeneratedAnswer("synthetic external-mode test only", GeneratedAnswer.Mode.EXTERNAL),
                SILENT, (q, k, s) -> List.of(CURRENT));
        var result = service.ask(COMMAND);
        assertEquals(AnswerResult.Outcome.GENERATED_ANSWER, result.outcome());
        assertEquals("EXTERNAL", result.generationMode());
    }

    @Test void validatesQuestionAndLimitsWithoutFrameworkDependencies() {
        assertThrows(IllegalArgumentException.class, () -> new AskQuestionCommand(ACTOR, " ", 5));
        assertThrows(IllegalArgumentException.class, () -> new AskQuestionCommand(ACTOR, "a".repeat(4001), 5));
        assertThrows(IllegalArgumentException.class, () -> new AskQuestionCommand(ACTOR, "q", 0));
        assertThrows(IllegalArgumentException.class, () -> new AskQuestionCommand(ACTOR, "q", 21));
        assertThrows(NullPointerException.class, () -> new AskQuestionCommand(null, "q", 1));
        assertDoesNotThrow(() -> new AskQuestionCommand(ACTOR, "a".repeat(4000), 20));
    }

    private static QuestionService service(GenerateAnswerPort generation, ObservationPort observation, RetrieveEvidencePort retrieval) {
        return new QuestionService(retrieval, generation,
                (tenant, id) -> Optional.of(document("current", 1, Document.Status.ACTIVE)), policy(Set.of("current")), observation);
    }

    private static AccessPolicyPort policy(Set<String> allowed) {
        return new AccessPolicyPort() {
            @Override public SearchScope scopeFor(Actor actor) { return new SearchScope(actor.tenantId(), allowed); }
            @Override public boolean canRead(Actor actor, Document document) { return !document.id().equals("revoked"); }
        };
    }

    private static DocumentCatalogPort emptyCatalog() { return (tenant, id) -> Optional.empty(); }
    private static Evidence evidence(String id, String tenant, long version) { return new Evidence(id, tenant, version, "fixture:1", "synthetic text"); }
    private static Document document(String id, long version, Document.Status status) { return new Document(id, "tenant", version, status); }
}
