package io.github.ragportfolio.adapters;

import io.github.ragportfolio.adapters.external.ElasticsearchRetrievalAdapter;
import io.github.ragportfolio.adapters.external.ExternalLlmAdapter;
import io.github.ragportfolio.adapters.external.PageIndexRetrievalAdapter;
import io.github.ragportfolio.application.AdapterNotImplementedException;
import io.github.ragportfolio.application.port.SearchScope;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ExternalAdapterContractTest {
    @Test void placeholdersFailExplicitlyWithoutExternalCalls() {
        var scope = new SearchScope("tenant", Set.of("doc"));
        assertThrows(AdapterNotImplementedException.class, () -> new ElasticsearchRetrievalAdapter().retrieve("q", 1, scope));
        assertThrows(AdapterNotImplementedException.class, () -> new PageIndexRetrievalAdapter().retrieve("q", 1, scope));
        assertThrows(AdapterNotImplementedException.class, () -> new ExternalLlmAdapter().generate("q", List.of()));
    }
}
