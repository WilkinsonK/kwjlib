package org.kwj.monoid;

public interface FnTransformI<From, Into> {
    Into transform(final From from);
}
