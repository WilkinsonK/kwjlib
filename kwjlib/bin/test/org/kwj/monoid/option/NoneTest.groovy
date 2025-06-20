package org.kwj.monoid.option

import org.kwj.monoid.*
import spock.lang.Specification

class NoneTest extends Specification {

    def "instance returns singleton"() {
        expect:
        None.instance().is(None.instance())
    }

    def "instance returns None type"() {
        given:
        def none = None.instance()

        expect:
        none instanceof None
        none instanceof Option
    }

    def "isSome returns false"() {
        given:
        def none = None.instance()

        expect:
        !none.isSome()
    }

    def "isNone returns true"() {
        given:
        def none = None.instance()

        expect:
        none.isNone()
    }

    def "unwrap throws UnwrapException"() {
        given:
        def none = None.instance()

        when:
        none.unwrap()

        then:
        thrown(UnwrapException)
    }

    def "unwrapUnsafe returns correct Unwrapped with error"() {
        given:
        def none = None.instance()

        when:
        def unwrapped = none.unwrapUnsafe()

        then:
        unwrapped.getValue() == null
        unwrapped.getError() instanceof UnwrapNoneException
    }

    def "map returns None"() {
        given:
        def none = None.instance()
        def transformer = { String s -> s.length() } as FnTransformI

        when:
        def result = none.map(transformer)

        then:
        result.isNone()
        result.is(None.instance()) // Should be same singleton
    }

    def "filter returns None"() {
        given:
        def none = None.instance()
        def predicate = { String s -> s.length() > 0 } as FnPredicateI

        when:
        def result = none.filter(predicate)

        then:
        result.isNone()
        result.is(None.instance())
    }

    def "and returns None regardless of second argument"() {
        given:
        def none = None.instance()
        def some = new Some("value")

        when:
        def result1 = none.and(some)
        def result2 = none.and(none)

        then:
        result1.isNone()
        result2.isNone()
        result1.is(None.instance())
        result2.is(None.instance())
    }

    def "andThen returns None without calling function"() {
        given:
        def none = None.instance()
        def called = false
        def transformer = { String s ->
            called = true
            new Some(s.length())
        } as FnTransformI

        when:
        def result = none.andThen(transformer)

        then:
        result.isNone()
        !called // Function should not be called
        result.is(None.instance())
    }

    def "or returns the alternative option"() {
        given:
        def none = None.instance()
        def some = new Some("alternative")

        when:
        def result = none.or(some)

        then:
        result.isSome()
        result.unwrap() == "alternative"
    }

    def "or with None returns None"() {
        given:
        def none1 = None.instance()
        def none2 = None.instance()

        when:
        def result = none1.or(none2)

        then:
        result.isNone()
        result.is(None.instance())
    }

    def "orElse calls generator and returns result"() {
        given:
        def none = None.instance()
        def generator = { -> new Some("generated") } as FnGenerateI

        when:
        def result = none.orElse(generator)

        then:
        result.isSome()
        result.unwrap() == "generated"
    }

    def "orElse with generator returning None"() {
        given:
        def none = None.instance()
        def generator = { -> None.instance() } as FnGenerateI

        when:
        def result = none.orElse(generator)

        then:
        result.isNone()
        result.is(None.instance())
    }

    def "isSomeAnd returns false without calling predicate"() {
        given:
        def none = None.instance()
        def called = false
        def predicate = { String s ->
            called = true
            s.length() > 0
        } as FnPredicateI

        when:
        def result = none.isSomeAnd(predicate)

        then:
        !result
        !called // Predicate should not be called
    }

    def "unwrapOr returns the default value"() {
        given:
        def none = None.instance()

        when:
        def result = none.unwrapOr("default")

        then:
        result == "default"
    }

    def "unwrapOrElse calls generator and returns result"() {
        given:
        def none = None.instance()
        def generator = { -> "generated" } as FnGenerateI

        when:
        def result = none.unwrapOrElse(generator)

        then:
        result == "generated"
    }

    def "inspect doesn't call function and returns self"() {
        given:
        def none = None.instance()
        def called = false
        def inspector = { String s -> called = true } as FnInspectI

        when:
        def result = none.inspect(inspector)

        then:
        result.is(none)
        !called
    }

    def "inspectOrElse calls generator function"() {
        given:
        def none = None.instance()
        def inspectCalled = false
        def inspector = { String s -> inspectCalled = true } as FnInspectI
        def generator = { -> new Some("fallback") } as FnGenerateI

        when:
        def result = none.inspectOrElse(inspector, generator)

        then:
        result.isSome()
        result.unwrap() == "fallback"
        !inspectCalled // Inspect should not be called
    }

    def "chaining operations maintains None"() {
        given:
        def none = None.instance()

        when:
        def result = none
            .map({ String s -> s.toUpperCase() } as FnTransformI)
            .filter({ String s -> s.length() > 0 } as FnPredicateI)
            .map({ String s -> s.length() } as FnTransformI)

        then:
        result.isNone()
        result.is(None.instance())
    }

    def "chaining with or operation provides fallback"() {
        given:
        def none = None.instance()

        when:
        def result = none
            .map({ String s -> s.toUpperCase() } as FnTransformI)
            .or(new Some("fallback"))

        then:
        result.isSome()
        result.unwrap() == "fallback"
    }

    def "multiple None instances are same singleton"() {
        expect:
        None.instance().is(None.instance())
        None.<String>instance().is(None.<Integer>instance())
        None.<String>instance().is(None.<List<String>>instance())
    }

    def "thread safety - concurrent access to singleton"() {
        given:
        def results = Collections.synchronizedSet([] as Set)

        when:
        (1..100).collect { i ->
            Thread.start {
                def none = None.instance()
                results.add(System.identityHashCode(none))
            }
        }.each { it.join() }

        then:
        results.size() == 1 // All should be same object
    }

    def "immutability - all operations return None singleton"() {
        given:
        def none = None.instance()

        expect:
        none.map({ String s -> s } as FnTransformI).is(none)
        none.filter({ String s -> true } as FnPredicateI).is(none)
        none.and(new Some("test")).is(none)
        none.inspect({ String s -> } as FnInspectI).is(none)
    }

    def "None with different generic types"() {
        given:
        def noneString = None.<String>instance()
        def noneInt = None.<Integer>instance()
        def noneList = None.<List<String>>instance()

        expect:
        noneString.is(noneInt)
        noneInt.is(noneList)
        noneString.isNone()
        noneInt.isNone()
        noneList.isNone()
    }
}
