package org.kwj.monoid.result

import org.kwj.monoid.*
import org.kwj.monoid.option.None
import org.kwj.monoid.option.Some
import spock.lang.Specification

class ResultTest extends Specification {

    // Factory method tests
    def "Result.pass creates Passing when value is non-null"() {
        when:
        def result = Result.pass("hello")

        then:
        result instanceof Passing
        result.isPassing()
        !result.isFailing()
        result.unwrap() == "hello"
    }

    def "Result.pass rejects null values"() {
        when:
        Result.pass(null)

        then:
        thrown(NullPointerException)
    }

    def "Result.fail creates Failing with exception"() {
        given:
        def error = new RuntimeException("test error")

        when:
        def result = Result.fail(error)

        then:
        result instanceof Failing
        !result.isPassing()
        result.isFailing()
    }

    def "Result.fail accepts null exception"() {
        when:
        def result = Result.fail(null)

        then:
        result instanceof Failing
        result.isFailing()
    }

    def "Result.attempt creates Passing when function succeeds"() {
        given:
        def generator = { -> "success" } as FnGenerateI

        when:
        def result = Result.attempt(generator)

        then:
        result.isPassing()
        result.unwrap() == "success"
    }

    def "Result.attempt creates Failing when function throws exception"() {
        given:
        def generator = { -> throw new RuntimeException("error") } as FnGenerateI

        when:
        def result = Result.attempt(generator)

        then:
        result.isFailing()
        result.unwrapUnsafe().getError() instanceof RuntimeException
        result.unwrapUnsafe().getError().message == "error"
    }

    def "Result.attempt handles various types correctly"() {
        expect:
        Result.attempt({ -> 42 } as FnGenerateI).isPassing()
        Result.attempt({ -> "" } as FnGenerateI).isPassing()
        Result.attempt({ -> false } as FnGenerateI).isPassing()
        Result.attempt({ -> [] } as FnGenerateI).isPassing()
    }

    // Map operation tests
    def "map transforms Passing values"() {
        given:
        def passing = Result.pass("hello")
        def transformer = { String s -> s.length() } as FnTransformI

        when:
        def result = passing.map(transformer)

        then:
        result.isPassing()
        result.unwrap() == 5
    }

    def "map on Failing returns Failing with same error"() {
        given:
        def error = new RuntimeException("original error")
        def failing = Result.fail(error)
        def transformer = { String s -> s.length() } as FnTransformI

        when:
        def result = failing.map(transformer)

        then:
        result.isFailing()
        result.unwrapUnsafe().getError().is(error)
    }

    def "map catches exceptions and wraps them in Failing"() {
        given:
        def passing = Result.pass("hello")
        def transformer = { String s -> throw new IllegalArgumentException("transform error") } as FnTransformI

        when:
        def result = passing.map(transformer)

        then:
        result.isFailing()
        result.unwrapUnsafe().getError() instanceof IllegalArgumentException
        result.unwrapUnsafe().getError().message == "transform error"
    }

    def "map handles type transformation"() {
        given:
        def passing = Result.pass(42)
        def transformer = { Integer i -> i.toString() } as FnTransformI

        when:
        def result = passing.map(transformer)

        then:
        result.isPassing()
        result.unwrap() == "42"
    }

    // Filter operation tests
    def "filter keeps Passing when predicate is true"() {
        given:
        def passing = Result.pass(10)
        def predicate = { Integer i -> i > 5 } as FnPredicateI

        when:
        def result = passing.filter(predicate)

        then:
        result.isPassing()
        result.unwrap() == 10
    }

    def "filter returns Failing when predicate is false"() {
        given:
        def passing = Result.pass(3)
        def predicate = { Integer i -> i > 5 } as FnPredicateI

        when:
        def result = passing.filter(predicate)

        then:
        result.isFailing()
    }

    def "filter on Failing returns same Failing"() {
        given:
        def error = new RuntimeException("test error")
        def failing = Result.fail(error)
        def predicate = { Integer i -> i > 5 } as FnPredicateI

        when:
        def result = failing.filter(predicate)

        then:
        result.isFailing()
        result.unwrapUnsafe().getError().is(error)
    }

    // AndThen operation tests
    def "andThen chains transformations for Passing"() {
        given:
        def passing = Result.pass("hello")
        def transformer = { String s -> Result.pass(s.length()) } as FnTransformI

        when:
        def result = passing.andThen(transformer)

        then:
        result.isPassing()
        result.unwrap() == 5
    }

    def "andThen returns Failing for Failing input"() {
        given:
        def error = new RuntimeException("original error")
        def failing = Result.fail(error)
        def transformer = { String s -> Result.pass(s.length()) } as FnTransformI

        when:
        def result = failing.andThen(transformer)

        then:
        result.isFailing()
        result.unwrapUnsafe().getError().is(error)
    }

    def "andThen can return Failing from transformation"() {
        given:
        def passing = Result.pass("test")
        def error = new IllegalStateException("transform failed")
        def transformer = { String s -> Result.fail(error) } as FnTransformI

        when:
        def result = passing.andThen(transformer)

        then:
        result.isFailing()
        result.unwrapUnsafe().getError().is(error)
    }

    // Or operation tests
    def "or returns first result when it's Passing"() {
        given:
        def passing = Result.pass("first")
        def other = Result.pass("second")

        when:
        def result = passing.or(other)

        then:
        result.isPassing()
        result.unwrap() == "first"
    }

    def "or returns second result when first is Failing"() {
        given:
        def failing = Result.fail(new RuntimeException("error"))
        def passing = Result.pass("fallback")

        when:
        def result = failing.or(passing)

        then:
        result.isPassing()
        result.unwrap() == "fallback"
    }

    def "or returns second result even when both are Failing"() {
        given:
        def error1 = new RuntimeException("error1")
        def error2 = new IllegalStateException("error2")
        def failing1 = Result.fail(error1)
        def failing2 = Result.fail(error2)

        when:
        def result = failing1.or(failing2)

        then:
        result.isFailing()
        result.unwrapUnsafe().getError().is(error2)
    }

    // OrElse operation tests
    def "orElse returns original when it's Passing"() {
        given:
        def passing = Result.pass("original")
        def generator = { -> Result.pass("generated") } as FnGenerateI

        when:
        def result = passing.orElse(generator)

        then:
        result.isPassing()
        result.unwrap() == "original"
    }

    def "orElse calls generator when original is Failing"() {
        given:
        def failing = Result.fail(new RuntimeException("error"))
        def generator = { -> Result.pass("generated") } as FnGenerateI

        when:
        def result = failing.orElse(generator)

        then:
        result.isPassing()
        result.unwrap() == "generated"
    }

    // IsPassingAnd operation tests
    def "isPassingAnd returns true when Passing and predicate true"() {
        given:
        def passing = Result.pass(15)
        def predicate = { Integer i -> i > 10 } as FnPredicateI

        expect:
        passing.isPassingAnd(predicate)
    }

    def "isPassingAnd returns false when Passing but predicate false"() {
        given:
        def passing = Result.pass(5)
        def predicate = { Integer i -> i > 10 } as FnPredicateI

        expect:
        !passing.isPassingAnd(predicate)
    }

    def "isPassingAnd returns false when Failing"() {
        given:
        def failing = Result.fail(new RuntimeException("error"))
        def predicate = { Integer i -> i > 10 } as FnPredicateI

        expect:
        !failing.isPassingAnd(predicate)
    }

    // UnwrapOr tests
    def "unwrapOr returns value for Passing"() {
        given:
        def passing = Result.pass("actual")

        when:
        def result = passing.unwrapOr("default")

        then:
        result == "actual"
    }

    def "unwrapOr returns default for Failing"() {
        given:
        def failing = Result.fail(new RuntimeException("error"))

        when:
        def result = failing.unwrapOr("default")

        then:
        result == "default"
    }

    // UnwrapOrElse tests
    def "unwrapOrElse returns value for Passing"() {
        given:
        def passing = Result.pass("actual")
        def generator = { -> "generated" } as FnGenerateI

        when:
        def result = passing.unwrapOrElse(generator)

        then:
        result == "actual"
    }

    def "unwrapOrElse calls generator for Failing"() {
        given:
        def failing = Result.fail(new RuntimeException("error"))
        def generator = { -> "generated" } as FnGenerateI

        when:
        def result = failing.unwrapOrElse(generator)

        then:
        result == "generated"
    }

    def "unwrapOrElse handles generator exceptions"() {
        given:
        def failing = Result.fail(new RuntimeException("original error"))
        def generator = { -> throw new IllegalStateException("generator error") } as FnGenerateI

        when:
        failing.unwrapOrElse(generator)

        then:
        UnwrapException ex = thrown()
        ex.cause instanceof IllegalStateException
        ex.cause.message == "generator error"
    }

    // Passing method tests
    def "passing returns Some for Passing result"() {
        given:
        def passing = Result.pass("value")

        when:
        def option = passing.passing()

        then:
        option instanceof Some
        option.unwrap() == "value"
    }

    def "passing returns None for Failing result"() {
        given:
        def failing = Result.fail(new RuntimeException("error"))

        when:
        def option = failing.passing()

        then:
        option instanceof None
        option.isNone()
    }

    // Inspect operation tests
    def "inspect calls function on Passing and returns self"() {
        given:
        def passing = Result.pass("test")
        def inspected = []
        def inspector = { String s -> inspected.add(s) } as FnInspectI

        when:
        def result = passing.inspect(inspector)

        then:
        result.is(passing)
        inspected == ["test"]
    }

    def "inspect doesn't call function on Failing but returns self"() {
        given:
        def error = new RuntimeException("error")
        def failing = Result.fail(error)
        def inspected = []
        def inspector = { String s -> inspected.add(s) } as FnInspectI

        when:
        def result = failing.inspect(inspector)

        then:
        result.is(failing)
        inspected.isEmpty()
    }

    // Exception handling tests
    def "unwrap throws UnwrapFailException for Failing"() {
        given:
        def originalError = new RuntimeException("original")
        def failing = Result.fail(originalError)

        when:
        failing.unwrap()

        then:
        UnwrapFailException ex = thrown()
        ex.cause.is(originalError)
    }

    // Edge cases and error conditions
    def "chaining operations works correctly"() {
        given:
        def result = Result.pass("hello")

        when:
        def finalResult = result
            .map({ String s -> s.toUpperCase() } as FnTransformI)
            .filter({ String s -> s.length() > 3 } as FnPredicateI)
            .map({ String s -> s.length() } as FnTransformI)

        then:
        finalResult.isPassing()
        finalResult.unwrap() == 5
    }

    def "chaining with Failing propagates correctly"() {
        given:
        def error = new RuntimeException("error")
        def result = Result.fail(error)

        when:
        def finalResult = result
            .map({ String s -> s.toUpperCase() } as FnTransformI)
            .filter({ String s -> s.length() > 3 } as FnPredicateI)
            .or(Result.pass("fallback"))

        then:
        finalResult.isPassing()
        finalResult.unwrap() == "fallback"
    }

    def "chaining that fails midway stops processing"() {
        given:
        def result = Result.pass("hello")

        when:
        def finalResult = result
            .map({ String s -> s.toUpperCase() } as FnTransformI)
            .filter({ String s -> s.length() < 3 } as FnPredicateI)  // This will fail
            .map({ String s -> s.length() } as FnTransformI)

        then:
        finalResult.isFailing()
    }

    def "complex chaining with recovery"() {
        given:
        def result = Result.pass(5)

        when:
        def finalResult = result
            .filter({ Integer i -> i > 10 } as FnPredicateI)  // This will fail
            .or(Result.pass(15))  // Recovery
            .map({ Integer i -> i * 2 } as FnTransformI)

        then:
        finalResult.isPassing()
        finalResult.unwrap() == 30
    }

    def "thread safety - concurrent access"() {
        given:
        def passing = Result.pass("shared")
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
}
