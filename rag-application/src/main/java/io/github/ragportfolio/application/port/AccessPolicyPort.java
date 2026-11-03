package io.github.ragportfolio.application.port;

import io.github.ragportfolio.domain.Actor;
import io.github.ragportfolio.domain.Document;

public interface AccessPolicyPort {
    SearchScope scopeFor(Actor actor);
    boolean canRead(Actor actor, Document document);
}
