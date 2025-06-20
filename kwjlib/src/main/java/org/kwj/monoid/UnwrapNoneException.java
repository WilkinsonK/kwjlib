package org.kwj.monoid;

public class UnwrapNoneException extends UnwrapException {

    public UnwrapNoneException() {
        super("Attempted to unwrap a `None` option.");
    }

    public UnwrapNoneException(String message) {
        super(message);
    }

    public UnwrapNoneException(String message, Throwable from) {
        super(message, from);
    }
}
