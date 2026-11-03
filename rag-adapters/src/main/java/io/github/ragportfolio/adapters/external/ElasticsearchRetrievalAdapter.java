package io.github.ragportfolio.adapters.external;

import io.github.ragportfolio.application.AdapterNotImplementedException;
import io.github.ragportfolio.application.port.RetrieveEvidencePort;
import io.github.ragportfolio.application.port.SearchScope;
import io.github.ragportfolio.domain.Evidence;
import java.util.List;

public final class ElasticsearchRetrievalAdapter implements RetrieveEvidencePort {
    @Override
    public List<Evidence> retrieve(String question, int topK, SearchScope scope) {
        throw new AdapterNotImplementedException("Elasticsearch retrieval");
    }
}
