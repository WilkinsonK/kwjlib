package org.kwj.monoid.option;

import org.kwj.monoid.FnGenerateI;
import org.kwj.monoid.FnInspectI;
import org.kwj.monoid.FnPredicateI;
import org.kwj.monoid.FnTransformI;
import org.kwj.monoid.UnwrapException;
import org.kwj.monoid.UnwrapNoneException;
import org.kwj.monoid.Unwrapped;

public abstract class Option<V> implements OptionI<V> {
    public static <U> OptionI<U> from(final U value) {
        if (value == null)
            return None.instance();
        return new Some<>(value);
    }

    public <U> OptionI<U> and(OptionI<U> that) {
        return this.isSome() && that.isSome() ? that : None.instance();
    }

    public <U> OptionI<U> andThen(FnTransformI<V, OptionI<U>> fn) {
        return isSome() ? fn.transform(unwrapUnsafe().getValue()) : None.instance();
    }

    public OptionI<V> filter(FnPredicateI<V> fn) {
        return isSomeAnd(fn) ? this : None.instance();
    }

    public OptionI<V> inspect(FnInspectI<V> fn) {
        if (isSome()) fn.inspect(unwrapUnsafe().getValue());
        return this;
    }

    public OptionI<V> inspectOrElse(FnInspectI<V> ifn, FnGenerateI<OptionI<V>> gfn) {
        return inspect(ifn).orElse(gfn);
    }

    public boolean isSome() {
        return this instanceof Some;
    }

    public boolean isSomeAnd(FnPredicateI<V> fn) {
        return isSome() && fn.isTrue(unwrapUnsafe().getValue());
    }

    public boolean isNone() {
        return this instanceof None;
    }

    public <U> OptionI<U> map(FnTransformI<V, U> fn) {
        return isSome() ? Option.from(fn.transform(unwrapUnsafe().getValue())) : None.instance();
    }

    public OptionI<V> or(OptionI<V> that) {
        return isSome() ? this : that;
    }

    public OptionI<V> orElse(FnGenerateI<OptionI<V>> fn) {
        return isSome() ? this : fn.generate();
    }

    public V unwrap() throws UnwrapException {
        if (isSome())
            return unwrapUnsafe().getValue();
        throw unwrapUnsafe().getError();
    }

    public V unwrapOr(V other) throws UnwrapException {
        return this.or(Option.from(other)).unwrap();
    }

    public V unwrapOrElse(FnGenerateI<V> fn) throws UnwrapException {
        return isSome() ? this.unwrap() : Option.from(fn.generate()).unwrap();
    }

    public Unwrapped<V, UnwrapNoneException> unwrapUnsafe() {
        return new Unwrapped<>(null, new UnwrapNoneException());
    }
}
