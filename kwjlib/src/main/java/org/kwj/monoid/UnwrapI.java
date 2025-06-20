package org.kwj.monoid;

/**
* Type is a container for either a value `V` or some `Exception` variant.
*/
public interface UnwrapI<V, E extends Exception> {
    /**
    * Unwrap the contained value.
    * @return `V`
    * @throws UnwrapException
    */
    V unwrap() throws UnwrapException;
    /**
    * Unwrap the contained value, if it is `Some`, otherwise returns the
    * `other`.
    * @param other
    * @return `V`
    * @throws UnwrapException
    */
    V unwrapOr(V other) throws UnwrapException;
    /**
    * Unwrap the contained value, if it is `Some`, otherwise generates a
    * default value to fallback on.
    * @param fn
    * @return `V`
    * @throws UnwrapException
    */
    V unwrapOrElse(FnGenerateI<V> fn) throws UnwrapException;
    /**
    * Returns the **RAW** representation of the unwrappable. This includes the
    * contained value-- if present-- and the exception that occured-- also if
    * present.
    * @return `Unwrapped<V, E>`
    */
    Unwrapped<V, E> unwrapUnsafe();
}
