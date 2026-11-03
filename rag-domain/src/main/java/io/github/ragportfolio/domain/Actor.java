package io.github.ragportfolio.domain;

public record Actor(String tenantId, String subjectId) {
    public Actor {
        if (tenantId == null || tenantId.isBlank() || subjectId == null || subjectId.isBlank()) {
            throw new IllegalArgumentException("tenantId and subjectId are required");
        }
    }
}
