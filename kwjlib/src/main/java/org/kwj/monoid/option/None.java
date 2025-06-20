package org.kwj.monoid.option;

public final class None<V> extends Option<V> {
    private static final None<?> INSTANCE = new None<>();

    private None() {}

    @SuppressWarnings("unchecked")
    public static <U> None<U> instance() {
        return (None<U>)INSTANCE;
    }
}
