package io.github.ragportfolio.application;

import java.util.List;
import java.util.Objects;

public record AnswerResult(Outcome outcome, String answer, List<Citation> citations, String generationMode) {
    public enum Outcome { MOCK_ANSWER, GENERATED_ANSWER, NO_CONTEXT }

    public AnswerResult {
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(answer, "answer");
        citations = List.copyOf(citations);
        Objects.requireNonNull(generationMode, "generationMode");
    }
}
