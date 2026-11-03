package io.github.ragportfolio.adapters.stub;

import io.github.ragportfolio.application.port.GenerateAnswerPort;
import io.github.ragportfolio.application.port.GeneratedAnswer;
import io.github.ragportfolio.domain.Evidence;
import java.util.List;

public final class MockGenerationAdapter implements GenerateAnswerPort {
    @Override
    public GeneratedAnswer generate(String question, List<Evidence> authorizedEvidence) {
        if (authorizedEvidence.isEmpty()) {
            throw new IllegalArgumentException("mock generation requires evidence");
        }
        return new GeneratedAnswer("[MOCK] Fixed local demonstration; no LLM was called.", GeneratedAnswer.Mode.MOCK);
    }
}
