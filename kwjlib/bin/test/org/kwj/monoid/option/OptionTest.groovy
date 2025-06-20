package org.kwj.monoid.option

import org.kwj.monoid.*
import spock.lang.Specification

class OptionTest extends Specification {

    // Factory method tests
    def "Option.from creates Some when value is non-null"() {
        when:
        def option = Option.from("hello")

        then:
        option instanceof Some
        option.isSome()
        !option.isNone()
        option.unwrap() == "hello"
    }

    def "Option.from creates None when value is null"() {
        when:
        def option = Option.from(null)

        then:
        option instanceof None
        !option.isSome()
        option.isNone()
    }

    def "Option.from handles various types correctly"() {
        expect:
        Option.from(42).isSome()
        Option.from(0).isSome()
        Option.from("").isSome()
        Option.from(false).isSome()
        Option.from([]).isSome()
        Option.from(null).isNone()
    }

    // Some type tests
    def "Some constructor rejects null values"() {
        when:
        new Some(null)

        then:
        thrown(NullPointerException)
    }

    def "Some stores and retrieves values correctly"() {
        given:
        def some = new Some("test value")

        expect:
        some.isSome()
        !some.isNone()
        some.unwrap() == "test value"
    }

    def "Some unwrapUnsafe returns correct Unwrapped"() {
        given:
        def some = new Some(123)
        def unwrapped = some.unwrapUnsafe()

        expect:
        unwrapped.getValue() == 123
        unwrapped.getError() == null
    }

    // None type tests
    def "None.instance returns singleton"() {
        expect:
        None.instance() is None.instance()
        None.instance() instanceof None
    }

    def "None has correct state"() {
        given:
        def none = None.instance()

        expect:
        !none.isSome()
        none.isNone()
    }

    def "None unwrap throws exception"() {
        given:
        def none = None.instance()

        when:
        none.unwrap()

        then:
        thrown(UnwrapException)
    }

    def "None unwrapUnsafe returns correct Unwrapped"() {
        given:
        def none = None.instance()
        def unwrapped = none.unwrapUnsafe()

        expect:
        unwrapped.getValue() == null
        unwrapped.getError() instanceof UnwrapNoneException
    }

    // Map operation tests
    def "map transforms Some values"() {
        given:
        def some = Option.from("hello")
        def transformer = { String s -> s.length() } as FnTransformI

        when:
        def result = some.map(transformer)

        then:
        result.isSome()
        result.unwrap() == 5
    }

    def "map on None returns None"() {
        given:
        def none = Option.from(null)
        def transformer = { String s -> s.length() } as FnTransformI

        when:
        def result = none.map(transformer)

        then:
        result.isNone()
    }

    def "map handles type transformation"() {
        given:
        def some = Option.from(42)
        def transformer = { Integer i -> i.toString() } as FnTransformI

        when:
        def result = some.map(transformer)

        then:
        result.isSome()
        result.unwrap() == "42"
    }

    // Filter operation tests
    def "filter keeps Some when predicate is true"() {
        given:
        def some = Option.from(10)
        def predicate = { Integer i -> i > 5 } as FnPredicateI

        when:
        def result = some.filter(predicate)

        then:
        result.isSome()
        result.unwrap() == 10
    }

    def "filter returns None when predicate is false"() {
        given:
        def some = Option.from(3)
        def predicate = { Integer i -> i > 5 } as FnPredicateI

        when:
        def result = some.filter(predicate)

        then:
        result.isNone()
    }

    def "filter on None returns None"() {
        given:
        def none = Option.from(null)
        def predicate = { Integer i -> i > 5 } as FnPredicateI

        when:
        def result = none.filter(predicate)

        then:
        result.isNone()
    }

    // And operation tests
    def "and returns second option when both are Some"() {
        given:
        def some1 = Option.from("first")
        def some2 = Option.from("second")

        when:
        def result = some1.and(some2)

        then:
        result.isSome()
        result.unwrap() == "second"
    }

    def "and returns None when first is None"() {
        given:
        def none = Option.from(null)
        def some = Option.from("value")

        when:
        def result = none.and(some)

        then:
        result.isNone()
    }

    def "and returns None when second is None"() {
        given:
        def some = Option.from("value")
        def none = Option.from(null)

        when:
        def result = some.and(none)

        then:
        result.isNone()
    }

    // AndThen operation tests
    def "andThen chains transformations for Some"() {
        given:
        def some = Option.from("hello")
        def transformer = { String s -> Option.from(s.length()) } as FnTransformI

        when:
        def result = some.andThen(transformer)

        then:
        result.isSome()
        result.unwrap() == 5
    }

    def "andThen returns None for None input"() {
        given:
        def none = Option.from(null)
        def transformer = { String s -> Option.from(s.length()) } as FnTransformI

        when:
        def result = none.andThen(transformer)

        then:
        result.isNone()
    }

    def "andThen can return None from transformation"() {
        given:
        def some = Option.from("test")
        def transformer = { String s -> Option.from(null) } as FnTransformI

        when:
        def result = some.andThen(transformer)

        then:
        result.isNone()
    }

    // Or operation tests
    def "or returns first option when it's Some"() {
        given:
        def some = Option.from("first")
        def other = Option.from("second")

        when:
        def result = some.or(other)

        then:
        result.isSome()
        result.unwrap() == "first"
    }

    def "or returns second option when first is None"() {
        given:
        def none = Option.from(null)
        def some = Option.from("fallback")

        when:
        def result = none.or(some)

        then:
        result.isSome()
        result.unwrap() == "fallback"
    }

    // OrElse operation tests
    def "orElse returns original when it's Some"() {
        given:
        def some = Option.from("original")
        def generator = { -> Option.from("generated") } as FnGenerateI

        when:
        def result = some.orElse(generator)

        then:
        result.isSome()
        result.unwrap() == "original"
    }

    def "orElse calls generator when original is None"() {
        given:
        def none = Option.from(null)
        def generator = { -> Option.from("generated") } as FnGenerateI

        when:
        def result = none.orElse(generator)

        then:
        result.isSome()
        result.unwrap() == "generated"
    }

    // IsSomeAnd operation tests
    def "isSomeAnd returns true when Some and predicate true"() {
        given:
        def some = Option.from(15)
        def predicate = { Integer i -> i > 10 } as FnPredicateI

        expect:
        some.isSomeAnd(predicate)
    }

    def "isSomeAnd returns false when Some but predicate false"() {
        given:
        def some = Option.from(5)
        def predicate = { Integer i -> i > 10 } as FnPredicateI

        expect:
        !some.isSomeAnd(predicate)
    }

    def "isSomeAnd returns false when None"() {
        given:
        def none = Option.from(null)
        def predicate = { Integer i -> i > 10 } as FnPredicateI

        expect:
        !none.isSomeAnd(predicate)
    }

    // UnwrapOr tests
    def "unwrapOr returns value for Some"() {
        given:
        def some = Option.from("actual")

        when:
        def result = some.unwrapOr("default")

        then:
        result == "actual"
    }

    def "unwrapOr returns default for None"() {
        given:
        def none = Option.from(null)

        when:
        def result = none.unwrapOr("default")

        then:
        result == "default"
    }

    // UnwrapOrElse tests
    def "unwrapOrElse returns value for Some"() {
        given:
        def some = Option.from("actual")
        def generator = { -> "generated" } as FnGenerateI

        when:
        def result = some.unwrapOrElse(generator)

        then:
        result == "actual"
    }

    def "unwrapOrElse calls generator for None"() {
        given:
        def none = Option.from(null)
        def generator = { -> "generated" } as FnGenerateI

        when:
        def result = none.unwrapOrElse(generator)

        then:
        result == "generated"
    }

    // Inspect operation tests
    def "inspect calls function on Some and returns self"() {
        given:
        def some = Option.from("test")
        def inspected = []
        def inspector = { String s -> inspected.add(s) } as FnInspectI

        when:
        def result = some.inspect(inspector)

        then:
        result.is(some)
        inspected == ["test"]
    }

    def "inspect doesn't call function on None but returns self"() {
        given:
        def none = Option.from(null)
        def inspected = []
        def inspector = { String s -> inspected.add(s) } as FnInspectI

        when:
        def result = none.inspect(inspector)

        then:
        result.is(none)
        inspected.isEmpty()
    }

    // Edge cases and error conditions
    def "chaining operations works correctly"() {
        given:
        def option = Option.from("hello")

        when:
        def result = option
            .map({ String s -> s.toUpperCase() } as FnTransformI)
            .filter({ String s -> s.length() > 3 } as FnPredicateI)
            .map({ String s -> s.length() } as FnTransformI)

        then:
        result.isSome()
        result.unwrap() == 5
    }

    def "chaining with None propagates correctly"() {
        given:
        def option = Option.from(null)

        when:
        def result = option
            .map({ String s -> s.toUpperCase() } as FnTransformI)
            .filter({ String s -> s.length() > 3 } as FnPredicateI)
            .or(Option.from("fallback"))

        then:
        result.isSome()
        result.unwrap() == "fallback"
    }

    def "equality and identity behavior"() {
        expect:
        None.instance().is(None.instance())
        new Some("test") != new Some("test") // Different instances
        Option.from("same").unwrap() == Option.from("same").unwrap()
    }
}
