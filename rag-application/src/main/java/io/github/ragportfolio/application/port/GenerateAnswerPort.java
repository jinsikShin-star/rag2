package io.github.ragportfolio.application.port;

import io.github.ragportfolio.domain.Evidence;
import java.util.List;

public interface GenerateAnswerPort {
    GeneratedAnswer generate(String question, List<Evidence> authorizedEvidence);
}
