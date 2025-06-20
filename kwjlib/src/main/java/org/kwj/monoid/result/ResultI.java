package org.kwj.monoid.result;

import org.kwj.monoid.FnInspectI;
import org.kwj.monoid.FnGenerateI;
import org.kwj.monoid.FnPredicateI;
import org.kwj.monoid.FnTransformI;
import org.kwj.monoid.UnwrapI;
import org.kwj.monoid.option.OptionI;

public interface ResultI<V> extends UnwrapI<V, Exception> {
    <U> ResultI<U> andThen(FnTransformI<V, ResultI<U>> fn);
    ResultI<V> filter(FnPredicateI<V> fn);
    ResultI<V> inspect(FnInspectI<V> fn);
    ResultI<V> inspectOrElse(FnInspectI<V> ifn, FnGenerateI<ResultI<V>> gfn);
    boolean isFailing();
    boolean isPassing();
    boolean isPassingAnd(FnPredicateI<V> fn);
    <U> ResultI<U> map(FnTransformI<V, U> fn);
    ResultI<V> or(ResultI<V> that);
    ResultI<V> orElse(FnGenerateI<ResultI<V>> fn);
    OptionI<V> passing();
}
