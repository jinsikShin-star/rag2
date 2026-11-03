package io.github.ragportfolio.application.port;

import io.github.ragportfolio.domain.Evidence;
import java.util.List;

public interface RetrieveEvidencePort {
    List<Evidence> retrieve(String question, int topK, SearchScope scope);
}
