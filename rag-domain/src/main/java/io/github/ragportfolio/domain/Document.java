package io.github.ragportfolio.domain;

import java.util.Objects;

public record Document(String id, String tenantId, long version, Status status) {
    public enum Status { ACTIVE, DELETED }

    public Document {
        if (id == null || id.isBlank() || tenantId == null || tenantId.isBlank() || version < 1) {
            throw new IllegalArgumentException("document identity, tenant and positive version are required");
        }
        Objects.requireNonNull(status, "status");
    }

    public boolean isCurrent(Evidence evidence, Actor actor) {
        return status == Status.ACTIVE && tenantId.equals(actor.tenantId())
                && id.equals(evidence.documentId()) && tenantId.equals(evidence.tenantId())
                && version == evidence.version();
    }
}
