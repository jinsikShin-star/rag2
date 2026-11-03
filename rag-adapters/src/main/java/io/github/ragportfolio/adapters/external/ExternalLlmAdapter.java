package io.github.ragportfolio.adapters.external;

import io.github.ragportfolio.application.AdapterNotImplementedException;
import io.github.ragportfolio.application.port.GenerateAnswerPort;
import io.github.ragportfolio.application.port.GeneratedAnswer;
import io.github.ragportfolio.domain.Evidence;
import java.util.List;

public final class ExternalLlmAdapter implements GenerateAnswerPort {
    @Override
    public GeneratedAnswer generate(String question, List<Evidence> authorizedEvidence) {
        throw new AdapterNotImplementedException("External LLM");
    }
}
