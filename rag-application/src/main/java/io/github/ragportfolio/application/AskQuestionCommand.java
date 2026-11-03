package io.github.ragportfolio.application;

import io.github.ragportfolio.domain.Actor;
import java.util.Objects;

public record AskQuestionCommand(Actor actor, String question, int topK) {
    public AskQuestionCommand {
        Objects.requireNonNull(actor, "actor");
        if (question == null || question.isBlank() || question.length() > 4000) {
            throw new IllegalArgumentException("question must contain 1..4000 characters");
        }
        if (topK < 1 || topK > 20) {
            throw new IllegalArgumentException("topK must be 1..20");
        }
    }
}
