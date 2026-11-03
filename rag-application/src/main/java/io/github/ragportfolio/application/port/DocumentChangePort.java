package io.github.ragportfolio.application.port;

import io.github.ragportfolio.domain.Document;

/** Future indexing boundary only; no persistence, worker or delivery guarantee is implemented. */
public interface DocumentChangePort {
    enum Kind { CONTENT_UPDATED, ACL_CHANGED, DELETED }
    void publish(Document document, Kind kind, String idempotencyKey);
}
