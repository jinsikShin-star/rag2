package io.github.ragportfolio.application.port;

import java.util.Set;

public record SearchScope(String tenantId, Set<String> documentIds) {
    public SearchScope {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId is required");
        }
        documentIds = Set.copyOf(documentIds);
        if (documentIds.stream().anyMatch(String::isBlank)) {
            throw new IllegalArgumentException("document ids cannot be blank");
        }
    }
}
