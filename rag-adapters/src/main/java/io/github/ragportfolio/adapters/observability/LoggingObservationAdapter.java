package io.github.ragportfolio.adapters.observability;

import io.github.ragportfolio.application.port.ObservationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoggingObservationAdapter implements ObservationPort {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingObservationAdapter.class);

    @Override
    public void completed(Outcome outcome, long elapsedNanos, int evidenceCount) {
        LOG.info("rag_demo outcome={} elapsedNanos={} evidenceCount={}", outcome, elapsedNanos, evidenceCount);
    }
}
