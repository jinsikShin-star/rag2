package io.github.ragportfolio.domain;

public record Evidence(String documentId, String tenantId, long version, String locator, String text) {
    public Evidence {
        if (documentId == null || documentId.isBlank() || tenantId == null || tenantId.isBlank()
                || version < 1 || locator == null || locator.isBlank() || text == null || text.isBlank()) {
            throw new IllegalArgumentException("evidence must include identity, positive version, locator and text");
        }
    }
}
