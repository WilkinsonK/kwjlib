package org.kwj.monoid;

public class UnwrapFailException extends UnwrapException {
    public UnwrapFailException(Throwable from) {
        super("Attempted to unwrap an error.", from);
    }

    public UnwrapFailException(String message) {
        super(message);
    }

    public UnwrapFailException(String message, Throwable from) {
        super(message, from);
    }
}
