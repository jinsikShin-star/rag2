package io.github.ragportfolio.application.port;

import java.util.Objects;

public record GeneratedAnswer(String text, Mode mode) {
    public enum Mode { MOCK, EXTERNAL }

    public GeneratedAnswer {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("generated text cannot be blank");
        }
        Objects.requireNonNull(mode, "mode");
    }
}
