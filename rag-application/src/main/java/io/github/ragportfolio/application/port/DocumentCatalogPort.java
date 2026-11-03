package io.github.ragportfolio.application.port;

import io.github.ragportfolio.domain.Document;
import java.util.Optional;

public interface DocumentCatalogPort {
    Optional<Document> findCurrent(String tenantId, String documentId);
}
