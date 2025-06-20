package org.kwj.monoid;

public final class Unwrapped<V, E extends Exception> {
    protected final V value;
    protected final E error;

    public Unwrapped(V value, E error) {
        this.value = value;
        this.error = error;
    }

    public V getValue() {
        return value;
    }

    public E getError() {
        return error;
    }
}
