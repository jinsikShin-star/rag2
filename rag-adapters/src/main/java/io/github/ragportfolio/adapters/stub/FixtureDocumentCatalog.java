package io.github.ragportfolio.adapters.stub;

import io.github.ragportfolio.application.port.DocumentCatalogPort;
import io.github.ragportfolio.domain.Document;
import java.util.Optional;

public final class FixtureDocumentCatalog implements DocumentCatalogPort {
    public static final Document DOCUMENT = new Document("demo-document", "demo-tenant", 1, Document.Status.ACTIVE);

    @Override
    public Optional<Document> findCurrent(String tenantId, String documentId) {
        return DOCUMENT.tenantId().equals(tenantId) && DOCUMENT.id().equals(documentId)
                ? Optional.of(DOCUMENT) : Optional.empty();
    }
}
