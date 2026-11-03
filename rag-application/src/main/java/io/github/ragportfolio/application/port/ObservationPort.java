package io.github.ragportfolio.application.port;

public interface ObservationPort {
    enum Outcome { ANSWER, NO_CONTEXT, ERROR }
    void completed(Outcome outcome, long elapsedNanos, int evidenceCount);
}
