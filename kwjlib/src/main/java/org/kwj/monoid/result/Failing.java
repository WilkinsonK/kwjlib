package org.kwj.monoid.result;

import org.kwj.monoid.Unwrapped;

public final class Failing<V> extends Result<V> {
    private final Exception error;

    public Failing(final Exception error) {
        this.error = error;
    }

    public Unwrapped<V, Exception> unwrapUnsafe() {
        return new Unwrapped<>(null, error);
    }
}
