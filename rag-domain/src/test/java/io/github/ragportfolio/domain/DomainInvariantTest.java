package io.github.ragportfolio.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DomainInvariantTest {
    @Test void rejectsInvalidIdentityAndVersion() {
        assertThrows(IllegalArgumentException.class, () -> new Actor(" ", "user"));
        assertThrows(IllegalArgumentException.class, () -> new Document("doc", "tenant", 0, Document.Status.ACTIVE));
        assertThrows(IllegalArgumentException.class, () -> new Evidence("doc", "tenant", 1, "page:1", ""));
    }

    @Test void currentEvidenceRequiresMatchingTenantVersionAndActiveState() {
        var actor = new Actor("tenant", "user");
        var evidence = new Evidence("doc", "tenant", 1, "page:1", "fixture");
        assertTrue(new Document("doc", "tenant", 1, Document.Status.ACTIVE).isCurrent(evidence, actor));
        assertFalse(new Document("doc", "tenant", 2, Document.Status.ACTIVE).isCurrent(evidence, actor));
        assertFalse(new Document("doc", "tenant", 1, Document.Status.DELETED).isCurrent(evidence, actor));
        assertFalse(new Document("doc", "tenant", 1, Document.Status.ACTIVE).isCurrent(evidence, new Actor("other", "user")));
    }
}
