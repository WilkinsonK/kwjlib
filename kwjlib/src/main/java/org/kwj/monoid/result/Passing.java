package org.kwj.monoid.result;

import java.util.Objects;

import org.kwj.monoid.Unwrapped;

public final class Passing<V> extends Result<V> {
    private final V value;

    public Passing(final V value) {
        this.value = Objects.requireNonNull(value);
    }

    public Unwrapped<V, Exception> unwrapUnsafe() {
        return new Unwrapped<>(value, null);
    }
}
