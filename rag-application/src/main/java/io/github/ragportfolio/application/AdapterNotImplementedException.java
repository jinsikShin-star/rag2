package io.github.ragportfolio.application;

public final class AdapterNotImplementedException extends UnsupportedOperationException {
    private static final long serialVersionUID = 1L;

    public AdapterNotImplementedException(String adapter) {
        super(adapter + " adapter is not implemented");
    }
}
