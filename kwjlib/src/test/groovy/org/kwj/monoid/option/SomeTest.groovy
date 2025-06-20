package org.kwj.monoid.option

import org.kwj.monoid.*
import spock.lang.Specification

class SomeTest extends Specification {

    def "constructor accepts non-null values"() {
        when:
        def some = new Some("test")

        then:
        some.unwrap() == "test"
    }

    def "constructor rejects null values"() {
        when:
        new Some(null)

        then:
        thrown(NullPointerException)
    }

    def "constructor accepts various non-null types"() {
        expect:
        new Some(42).unwrap() == 42
        new Some(0).unwrap() == 0
        new Some("").unwrap() == ""
        new Some(false).unwrap() == false
        new Some([]).unwrap() == []
        new Some([1, 2, 3]).unwrap() == [1, 2, 3]
    }

    def "isSome returns true"() {
        given:
        def some = new Some("value")

        expect:
        some.isSome()
    }

    def "isNone returns false"() {
        given:
        def some = new Some("value")

        expect:
        !some.isNone()
    }

    def "unwrap returns the stored value"() {
        given:
        def value = "test value"
        def some = new Some(value)

        when:
        def result = some.unwrap()

        then:
        result == value
    }

    def "unwrapUnsafe returns correct Unwrapped with value and no error"() {
        given:
        def value = 123
        def some = new Some(value)

        when:
        def unwrapped = some.unwrapUnsafe()

        then:
        unwrapped.getValue() == value
        unwrapped.getError() == null
    }

    def "map transforms the value"() {
        given:
        def some = new Some("hello")
        def transformer = { String s -> s.length() } as FnTransformI

        when:
        def result = some.map(transformer)

        then:
        result.isSome()
        result.unwrap() == 5
    }

    def "map with type transformation"() {
        given:
        def some = new Some(42)
        def transformer = { Integer i -> "Number: ${i}" } as FnTransformI

        when:
        def result = some.map(transformer)

        then:
        result.isSome()
        result.unwrap() == "Number: 42"
    }

    def "filter keeps value when predicate is true"() {
        given:
        def some = new Some(10)
        def predicate = { Integer i -> i > 5 } as FnPredicateI

        when:
        def result = some.filter(predicate)

        then:
        result.isSome()
        result.unwrap() == 10
    }

    def "filter returns None when predicate is false"() {
        given:
        def some = new Some(3)
        def predicate = { Integer i -> i > 5 } as FnPredicateI

        when:
        def result = some.filter(predicate)

        then:
        result.isNone()
    }

    def "and returns second option when both are Some"() {
        given:
        def some1 = new Some("first")
        def some2 = new Some("second")

        when:
        def result = some1.and(some2)

        then:
        result.isSome()
        result.unwrap() == "second"
    }

    def "and returns None when second is None"() {
        given:
        def some = new Some("value")
        def none = None.instance()

        when:
        def result = some.and(none)

        then:
        result.isNone()
    }

    def "andThen chains transformation"() {
        given:
        def some = new Some("hello")
        def transformer = { String s -> new Some(s.length()) } as FnTransformI

        when:
        def result = some.andThen(transformer)

        then:
        result.isSome()
        result.unwrap() == 5
    }

    def "andThen returns None when transformation returns None"() {
        given:
        def some = new Some("hello")
        def transformer = { String s -> None.instance() } as FnTransformI

        when:
        def result = some.andThen(transformer)

        then:
        result.isNone()
    }

    def "or returns self when Some"() {
        given:
        def some = new Some("original")
        def other = new Some("alternative")

        when:
        def result = some.or(other)

        then:
        result.is(some)
        result.unwrap() == "original"
    }

    def "orElse returns self when Some"() {
        given:
        def some = new Some("original")
        def generator = { -> new Some("generated") } as FnGenerateI

        when:
        def result = some.orElse(generator)

        then:
        result.is(some)
        result.unwrap() == "original"
    }

    def "isSomeAnd returns true when predicate is true"() {
        given:
        def some = new Some(15)
        def predicate = { Integer i -> i > 10 } as FnPredicateI

        expect:
        some.isSomeAnd(predicate)
    }

    def "isSomeAnd returns false when predicate is false"() {
        given:
        def some = new Some(5)
        def predicate = { Integer i -> i > 10 } as FnPredicateI

        expect:
        !some.isSomeAnd(predicate)
    }

    def "unwrapOr returns the contained value"() {
        given:
        def some = new Some("actual")

        when:
        def result = some.unwrapOr("default")

        then:
        result == "actual"
    }

    def "unwrapOrElse returns the contained value"() {
        given:
        def some = new Some("actual")
        def generator = { -> "generated" } as FnGenerateI

        when:
        def result = some.unwrapOrElse(generator)

        then:
        result == "actual"
    }

    def "inspect calls function and returns self"() {
        given:
        def some = new Some("test")
        def inspected = []
        def inspector = { String s -> inspected.add(s) } as FnInspectI

        when:
        def result = some.inspect(inspector)

        then:
        result.is(some)
        inspected == ["test"]
    }

    def "inspectOrElse calls inspect function and returns self"() {
        given:
        def some = new Some("test")
        def inspected = []
        def inspector = { String s -> inspected.add(s) } as FnInspectI
        def generator = { -> None.instance() } as FnGenerateI

        when:
        def result = some.inspectOrElse(inspector, generator)

        then:
        result.is(some)
        inspected == ["test"]
    }

    def "immutability - operations return new instances"() {
        given:
        def original = new Some("original")

        when:
        def mapped = original.map({ String s -> s.toUpperCase() } as FnTransformI)
        def filtered = original.filter({ String s -> s.length() > 0 } as FnPredicateI)

        then:
        original.unwrap() == "original" // Original unchanged
        mapped.unwrap() == "ORIGINAL"
        filtered.is(original) // Filter returns same when predicate true
    }

    def "thread safety - concurrent access"() {
        given:
        def some = new Some("shared")
        def results = Collections.synchronizedList([])

        when:
        (1..100).collect { i ->
            Thread.start {
                def result = some.map({ String s -> "${s}-${i}" } as FnTransformI)
                results.add(result.unwrap())
            }
        }.each { it.join() }

        then:
        results.size() == 100
        results.every { it.startsWith("shared-") }
        some.unwrap() == "shared" // Original unchanged
    }
}
