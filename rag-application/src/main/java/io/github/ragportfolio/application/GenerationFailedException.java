package io.github.ragportfolio.application;

public final class GenerationFailedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public GenerationFailedException(Throwable cause) {
        super("Answer generation failed", cause);
    }
}
