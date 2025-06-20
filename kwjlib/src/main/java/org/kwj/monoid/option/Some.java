package org.kwj.monoid.option;

import java.util.Objects;

import org.kwj.monoid.UnwrapNoneException;
import org.kwj.monoid.Unwrapped;

public final class Some<V> extends Option<V> {
    private final V value;

    public Some(final V value) {
        this.value = Objects.requireNonNull(value);
    }

    public Unwrapped<V, UnwrapNoneException> unwrapUnsafe() {
        return new Unwrapped<>(value, null);
    }
}
