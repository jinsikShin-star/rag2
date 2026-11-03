package io.github.ragportfolio.adapters.stub;

import io.github.ragportfolio.application.port.AccessPolicyPort;
import io.github.ragportfolio.application.port.SearchScope;
import io.github.ragportfolio.domain.Actor;
import io.github.ragportfolio.domain.Document;
import java.util.Set;

/** Fixture policy, not authentication or a production authorization mechanism. */
public final class DemoAccessPolicy implements AccessPolicyPort {
    @Override
    public SearchScope scopeFor(Actor actor) {
        return new SearchScope(actor.tenantId(), isDemoActor(actor) ? Set.of("demo-document") : Set.of());
    }

    @Override
    public boolean canRead(Actor actor, Document document) {
        return isDemoActor(actor) && document.tenantId().equals(actor.tenantId()) && document.id().equals("demo-document");
    }

    private static boolean isDemoActor(Actor actor) {
        return actor.tenantId().equals("demo-tenant") && actor.subjectId().equals("demo-user");
    }
}
