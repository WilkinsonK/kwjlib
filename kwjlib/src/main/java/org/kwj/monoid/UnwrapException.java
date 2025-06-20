package org.kwj.monoid;

public class UnwrapException extends Exception {
    public UnwrapException(String message) {
        super(message);
    }

    public UnwrapException(String message, Throwable from) {
        super(message, from);
    }
}
