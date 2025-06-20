package org.kwj.monoid.result;

import org.kwj.monoid.FnGenerateI;
import org.kwj.monoid.FnInspectI;
import org.kwj.monoid.FnPredicateI;
import org.kwj.monoid.FnTransformI;
import org.kwj.monoid.UnwrapException;
import org.kwj.monoid.UnwrapFailException;
import org.kwj.monoid.option.None;
import org.kwj.monoid.option.Option;
import org.kwj.monoid.option.OptionI;

public abstract class Result<V> implements ResultI<V> {
    public static <U> ResultI<U> attempt(FnGenerateI<U> fn) {
        try {
            return Result.pass(fn.generate());
        } catch (Exception error) {
            return Result.fail(error);
        }
    }

    public static <U> ResultI<U> fail(Exception error) {
        return new Failing<>(error);
    }

    public static <U> ResultI<U> pass(U value) {
        return new Passing<>(value);
    }

    public <U> ResultI<U> andThen(FnTransformI<V, ResultI<U>> fn) {
        return isPassing() ? fn.transform(unwrapUnsafe().getValue()) : Result.fail(unwrapUnsafe().getError());
    }

    public ResultI<V> filter(FnPredicateI<V> fn) {
        return isPassingAnd(fn) ? this : Result.fail(unwrapUnsafe().getError());
    }

    public ResultI<V> inspect(FnInspectI<V> fn) {
        if (isPassing()) fn.inspect(unwrapUnsafe().getValue());
        return this;
    }

    public ResultI<V> inspectOrElse(FnInspectI<V> ifn, FnGenerateI<ResultI<V>> gfn) {
        return inspect(ifn).orElse(gfn);
    }

    public boolean isFailing() {
        return this instanceof Failing;
    }

    public boolean isPassing() {
        return this instanceof Passing;
    }

    public boolean isPassingAnd(FnPredicateI<V> fn) {
        return isPassing() && fn.isTrue(unwrapUnsafe().getValue());
    }

    public <U> ResultI<U> map(FnTransformI<V, U> fn) {
        return isPassing() ? Result.attempt(() -> fn.transform(unwrapUnsafe().getValue())) : Result.fail(unwrapUnsafe().getError());
    }

    public ResultI<V> or(ResultI<V> that) {
        return isPassing() ? this : that;
    }

    public ResultI<V> orElse(FnGenerateI<ResultI<V>> fn) {
        return isPassing() ? this : fn.generate();
    }

    public OptionI<V> passing() {
        return isPassing() ? Option.from(unwrapUnsafe().getValue()) : None.instance();
    }

    public V unwrap() throws UnwrapException {
        if (isPassing())
            return unwrapUnsafe().getValue();
        throw new UnwrapFailException(unwrapUnsafe().getError());
    }

    public V unwrapOr(V other) throws UnwrapException {
        return this.or(Result.pass(other)).unwrap();
    }

    public V unwrapOrElse(FnGenerateI<V> fn) throws UnwrapException {
        return (isPassing() ? this : Result.attempt(fn)).unwrap();
    }
}
