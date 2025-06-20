package org.kwj.monoid.result

import org.kwj.monoid.*
import org.kwj.monoid.option.Some
import spock.lang.Specification

class PassingTest extends Specification {

    def "constructor accepts non-null values"() {
        when:
        def passing = new Passing("test")

        then:
        passing.unwrap() == "test"
    }

    def "constructor rejects null values"() {
        when:
        new Passing(null)

        then:
        thrown(NullPointerException)
    }

    def "constructor accepts various non-null types"() {
        expect:
        new Passing(42).unwrap() == 42
        new Passing(0).unwrap() == 0
        new Passing("").unwrap() == ""
        new Passing(false).unwrap() == false
        new Passing([]).unwrap() == []
        new Passing([1, 2, 3]).unwrap() == [1, 2, 3]
    }

    def "isPassing returns true"() {
        given:
        def passing = new Passing("value")

        expect:
        passing.isPassing()
    }

    def "isFailing returns false"() {
        given:
        def passing = new Passing("value")

        expect:
        !passing.isFailing()
    }

    def "unwrap returns the stored value"() {
        given:
        def value = "test value"
        def passing = new Passing(value)

        when:
        def result = passing.unwrap()

        then:
        result == value
    }

    def "unwrapUnsafe returns correct Unwrapped with value and no error"() {
        given:
        def value = 123
        def passing = new Passing(value)

        when:
        def unwrapped = passing.unwrapUnsafe()

        then:
        unwrapped.getValue() == value
        unwrapped.getError() == null
    }

    def "map transforms the value"() {
        given:
        def passing = new Passing("hello")
        def transformer = { String s -> s.length() } as FnTransformI

        when:
        def result = passing.map(transformer)

        then:
        result.isPassing()
        result.unwrap() == 5
    }

    def "map with type transformation"() {
        given:
        def passing = new Passing(42)
        def transformer = { Integer i -> "Number: ${i}" } as FnTransformI

        when:
        def result = passing.map(transformer)

        then:
        result.isPassing()
        result.unwrap() == "Number: 42"
    }

    def "map catches exceptions and returns Failing"() {
        given:
        def passing = new Passing("hello")
        def transformer = { String s -> throw new RuntimeException("transform error") } as FnTransformI

        when:
        def result = passing.map(transformer)

        then:
        result.isFailing()
        result.unwrapUnsafe().getError() instanceof RuntimeException
        result.unwrapUnsafe().getError().message == "transform error"
    }

    def "filter keeps value when predicate is true"() {
        given:
        def passing = new Passing(10)
        def predicate = { Integer i -> i > 5 } as FnPredicateI

        when:
        def result = passing.filter(predicate)

        then:
        result.isPassing()
        result.unwrap() == 10
    }

    def "filter returns Failing when predicate is false"() {
        given:
        def passing = new Passing(3)
        def predicate = { Integer i -> i > 5 } as FnPredicateI

        when:
        def result = passing.filter(predicate)

        then:
        result.isFailing()
    }

    def "andThen chains transformation"() {
        given:
        def passing = new Passing("hello")
        def transformer = { String s -> new Passing(s.length()) } as FnTransformI

        when:
        def result = passing.andThen(transformer)

        then:
        result.isPassing()
        result.unwrap() == 5
    }

    def "andThen returns Failing when transformation returns Failing"() {
        given:
        def passing = new Passing("hello")
        def error = new RuntimeException("transform failed")
        def transformer = { String s -> new Failing(error) } as FnTransformI

        when:
        def result = passing.andThen(transformer)

        then:
        result.isFailing()
        result.unwrapUnsafe().getError().is(error)
    }

    def "or returns self when Passing"() {
        given:
        def passing = new Passing("original")
        def other = new Passing("alternative")

        when:
        def result = passing.or(other)

        then:
        result.is(passing)
        result.unwrap() == "original"
    }

    def "orElse returns self when Passing"() {
        given:
        def passing = new Passing("original")
        def generator = { -> new Passing("generated") } as FnGenerateI

        when:
        def result = passing.orElse(generator)

        then:
        result.is(passing)
        result.unwrap() == "original"
    }

    def "isPassingAnd returns true when predicate is true"() {
        given:
        def passing = new Passing(15)
        def predicate = { Integer i -> i > 10 } as FnPredicateI

        expect:
        passing.isPassingAnd(predicate)
    }

    def "isPassingAnd returns false when predicate is false"() {
        given:
        def passing = new Passing(5)
        def predicate = { Integer i -> i > 10 } as FnPredicateI

        expect:
        !passing.isPassingAnd(predicate)
    }

    def "unwrapOr returns the contained value"() {
        given:
        def passing = new Passing("actual")

        when:
        def result = passing.unwrapOr("default")

        then:
        result == "actual"
    }

    def "unwrapOrElse returns the contained value"() {
        given:
        def passing = new Passing("actual")
        def generator = { -> "generated" } as FnGenerateI

        when:
        def result = passing.unwrapOrElse(generator)

        then:
        result == "actual"
    }

    def "passing returns Some with the value"() {
        given:
        def passing = new Passing("test")

        when:
        def option = passing.passing()

        then:
        option instanceof Some
        option.unwrap() == "test"
    }

    def "inspect calls function and returns self"() {
        given:
        def passing = new Passing("test")
        def inspected = []
        def inspector = { String s -> inspected.add(s) } as FnInspectI

        when:
        def result = passing.inspect(inspector)

        then:
        result.is(passing)
        inspected == ["test"]
    }

    def "inspectOrElse calls inspect function and returns self"() {
        given:
        def passing = new Passing("test")
        def inspected = []
        def inspector = { String s -> inspected.add(s) } as FnInspectI
        def generator = { -> new Failing(new RuntimeException("error")) } as FnGenerateI

        when:
        def result = passing.inspectOrElse(inspector, generator)

        then:
        result.is(passing)
        inspected == ["test"]
    }

    def "immutability - operations return new instances"() {
        given:
        def original = new Passing("original")

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
        def passing = new Passing("shared")
        def results = Collections.synchronizedList([])

        when:
        (1..100).collect { i ->
            Thread.start {
                def result = passing.map({ String s -> "${s}-${i}" } as FnTransformI)
                results.add(result.unwrap())
            }
        }.each { it.join() }

        then:
        results.size() == 100
        results.every { it.startsWith("shared-") }
        passing.unwrap() == "shared" // Original unchanged
    }

    def "chaining multiple operations maintains value"() {
        given:
        def passing = new Passing("hello")

        when:
        def result = passing
            .map({ String s -> s.toUpperCase() } as FnTransformI)
            .filter({ String s -> s.length() > 3 } as FnPredicateI)
            .map({ String s -> s.length() } as FnTransformI)

        then:
        result.isPassing()
        result.unwrap() == 5
    }

    def "complex type transformations"() {
        given:
        def passing = new Passing([1, 2, 3, 4, 5])

        when:
        def result = passing
            .map({ List<Integer> list -> list.sum() } as FnTransformI)
            .filter({ Integer sum -> sum > 10 } as FnPredicateI)
            .map({ Integer sum -> "Sum: ${sum}" } as FnTransformI)

        then:
        result.isPassing()
        result.unwrap() == "Sum: 15"
    }

    def "exception handling in complex chains"() {
        given:
        def passing = new Passing("test")

        when:
        def result = passing
            .map({ String s -> s.toUpperCase() } as FnTransformI)
            .map({ String s -> throw new IllegalArgumentException("boom") } as FnTransformI)
            .map({ String s -> s.length() } as FnTransformI)

        then:
        result.isFailing()
        result.unwrapUnsafe().getError() instanceof IllegalArgumentException
        result.unwrapUnsafe().getError().message == "boom"
    }

    def "recovery from failed operations"() {
        given:
        def passing = new Passing(3)

        when:
        def result = passing
            .filter({ Integer i -> i > 10 } as FnPredicateI)  // This will fail
            .or(new Passing(15))  // Recovery
            .map({ Integer i -> i * 2 } as FnTransformI)

        then:
        result.isPassing()
        result.unwrap() == 30
    }
}
