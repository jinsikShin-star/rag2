package io.github.ragportfolio.adapters.stub;

import io.github.ragportfolio.application.port.RetrieveEvidencePort;
import io.github.ragportfolio.application.port.SearchScope;
import io.github.ragportfolio.domain.Evidence;
import java.util.List;

public final class FixtureRetrievalAdapter implements RetrieveEvidencePort {
    public static final Evidence EVIDENCE = new Evidence("demo-document", "demo-tenant", 1,
            "fixture:section-1", "Synthetic local fixture for the hexagonal RAG architecture skeleton.");

    @Override
    public List<Evidence> retrieve(String question, int topK, SearchScope scope) {
        // shortcut: fixed fixture ignores query relevance; replace only after a real retrieval adapter is evaluated.
        return EVIDENCE.tenantId().equals(scope.tenantId()) && scope.documentIds().contains(EVIDENCE.documentId())
                ? List.of(EVIDENCE) : List.of();
    }
}
