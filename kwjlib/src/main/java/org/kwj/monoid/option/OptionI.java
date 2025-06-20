package org.kwj.monoid.option;

import org.kwj.monoid.FnGenerateI;
import org.kwj.monoid.FnInspectI;
import org.kwj.monoid.FnPredicateI;
import org.kwj.monoid.FnTransformI;
import org.kwj.monoid.UnwrapI;
import org.kwj.monoid.UnwrapNoneException;

/**
* A monoid determined to circumvent the headaches of `NullPointerException`s
* where they are least welcome. Basically everywhere.
*
* Provides an interface for interacting with contained values, depending on if
* they exist or not, before they are needed elsewhere.
*/
public interface OptionI<V> extends UnwrapI<V, UnwrapNoneException> {
    /**
    * Returns the `U` option if both `Option<V>` and `Option<U>` are `Some`.
    * Otherwise, returns `None`.
    * @param <U>
    * @param that
    * @return `OptionI<U>` if both `this` and `that` are `Some`.
    */
    <U> OptionI<U> and(OptionI<U> that);
    /**
    * Creates a new `Option<U>` from this `Option` if `this` is `Some`.
    * @param <U>
    * @param fn
    * @return `OptionI<U>` if `this` is `Some`.
    */
    <U> OptionI<U> andThen(FnTransformI<V, OptionI<U>> fn);
    /**
    * Returns the `Some` of this `Option` if it is `Some` and the contained
    * value meets the predicate.
    * @param fn
    * @return `OptionI<V>` if `this` is `Some` and `fn` is `true`.
    */
    OptionI<V> filter(FnPredicateI<V> fn);
    /**
    * Evaluate the contained value of a `Some` option.
    * @param fn
    * @return `OptionI<V>`
    */
    OptionI<V> inspect(FnInspectI<V> fn);
    /**
    * Evaluate the contained value of a `Some` option. If the `Option` is
    * `None`, generate an `Option` to replace it.
    * @param ifn
    * @param gfn
    * @return `OptionI<V>`
    */
    OptionI<V> inspectOrElse(FnInspectI<V> ifn, FnGenerateI<OptionI<V>> gfn);
    /**
    * This `Option` is a `Some` variant.
    * @return `boolean`
    */
    boolean isSome();
    /**
    * This `Option` is a `Some` variant and the contained value meets a
    * predicate.
    * @param fn
    * @return `boolean`
    */
    boolean isSomeAnd(FnPredicateI<V> fn);
    /**
    * This `Option` is a `None` variant.
    * @return `boolean`
    */
    boolean isNone();
    /**
    * Transform the contained value into another value.
    * @param <U>
    * @param fn
    * @return `OptionI<U>`
    */
    <U> OptionI<U> map(FnTransformI<V, U> fn);
    /**
    * Returns `Option` A if A is `Some` or returns `Option` B if A is `None`.
    * @param that
    * @return `OptionI<V>`
    */
    OptionI<V> or(OptionI<V> that);
    /**
    * Returns the `Option` if it is `Some`, otherwise generates a new `Option`
    * to replace it.
    * @param fn
    * @return `Option<V>`
 */
    OptionI<V> orElse(FnGenerateI<OptionI<V>> fn);
}
