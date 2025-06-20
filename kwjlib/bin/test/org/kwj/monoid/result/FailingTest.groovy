package org.kwj.monoid.result

import org.kwj.monoid.*
import org.kwj.monoid.option.None
import spock.lang.Specification

class FailingTest extends Specification {

    def "constructor accepts exception"() {
        given:
        def error = new RuntimeException("test error")

        when:
        def failing = new Failing(error)

        then:
        failing.unwrapUnsafe().getError().is(error)
    }

    def "constructor accepts null exception"() {
        when:
        def failing = new Failing(null)

        then:
        failing.unwrapUnsafe().getError() == null
    }

    def "constructor accepts various exception types"() {
        expect:
        new Failing(new RuntimeException("runtime")).unwrapUnsafe().getError() instanceof RuntimeException
        new Failing(new IllegalArgumentException("illegal")).unwrapUnsafe().getError() instanceof IllegalArgumentException
        new Failing(new NullPointerException("null")).unwrapUnsafe().getError() instanceof NullPointerException
        new Failing(new Exception("generic")).unwrapUnsafe().getError() instanceof Exception
    }

    def "isPassing returns false"() {
        given:
        def failing = new Failing(new RuntimeException("error"))

        expect:
        !failing.isPassing()
    }

    def "isFailing returns true"() {
        given:
        def failing = new Failing(new RuntimeException("error"))

        expect:
        failing.isFailing()
    }

    def "unwrap throws UnwrapFailException"() {
        given:
        def originalError = new RuntimeException("original error")
        def failing = new Failing(originalError)

        when:
        failing.unwrap()

        then:
        UnwrapFailException ex = thrown()
        ex.cause.is(originalError)
    }

    def "unwrapUnsafe returns correct Unwrapped with error and no value"() {
        given:
        def error = new IllegalStateException("test error")
        def failing = new Failing(error)

        when:
        def unwrapped = failing.unwrapUnsafe()

        then:
        unwrapped.getValue() == null
        unwrapped.getError().is(error)
    }

    def "map returns Failing with same error"() {
        given:
        def error = new RuntimeException("original error")
        def failing = new Failing(error)
        def transformer = { String s -> s.length() } as FnTransformI

        when:
        def result = failing.map(transformer)

        then:
        result.isFailing()
        result.unwrapUnsafe().getError().is(error)
    }

    def "map doesn't call transformation function"() {
        given:
        def error = new RuntimeException("error")
        def failing = new Failing(error)
        def called = false
        def transformer = { String s ->
            called = true
            s.length()
        } as FnTransformI

        when:
        def result = failing.map(transformer)

        then:
        result.isFailing()
        !called // Function should not be called
        result.unwrapUnsafe().getError().is(error)
    }

    def "filter returns Failing with same error"() {
        given:
        def error = new RuntimeException("original error")
        def failing = new Failing(error)
        def predicate = { String s -> s.length() > 0 } as FnPredicateI

        when:
        def result = failing.filter(predicate)

        then:
        result.isFailing()
        result.unwrapUnsafe().getError().is(error)
    }

    def "filter doesn't call predicate function"() {
        given:
        def error = new RuntimeException("error")
        def failing = new Failing(error)
        def called = false
        def predicate = { String s ->
            called = true
            s.length() > 0
        } as FnPredicateI

        when:
        def result = failing.filter(predicate)

        then:
        result.isFailing()
        !called // Function should not be called
        result.unwrapUnsafe().getError().is(error)
    }

    def "andThen returns Failing without calling function"() {
        given:
        def error = new RuntimeException("original error")
        def failing = new Failing(error)
        def called = false
        def transformer = { String s ->
            called = true
            new Passing(s.length())
        } as FnTransformI

        when:
        def result = failing.andThen(transformer)

        then:
        result.isFailing()
        !called // Function should not be called
        result.unwrapUnsafe().getError().is(error)
    }

    def "or returns the alternative result"() {
        given:
        def error = new RuntimeException("original error")
        def failing = new Failing(error)
        def alternative = new Passing("alternative")

        when:
        def result = failing.or(alternative)

        then:
        result.isPassing()
        result.unwrap() == "alternative"
    }

    def "or with Failing returns the second Failing"() {
        given:
        def error1 = new RuntimeException("error1")
        def error2 = new IllegalStateException("error2")
        def failing1 = new Failing(error1)
        def failing2 = new Failing(error2)

        when:
        def result = failing1.or(failing2)

        then:
        result.isFailing()
        result.unwrapUnsafe().getError().is(error2)
    }

    def "orElse calls generator and returns result"() {
        given:
        def error = new RuntimeException("original error")
        def failing = new Failing(error)
        def generator = { -> new Passing("generated") } as FnGenerateI

        when:
        def result = failing.orElse(generator)

        then:
        result.isPassing()
        result.unwrap() == "generated"
    }

    def "orElse with generator returning Failing"() {
        given:
        def error1 = new RuntimeException("original error")
        def error2 = new IllegalStateException("generated error")
        def failing = new Failing(error1)
        def generator = { -> new Failing(error2) } as FnGenerateI

        when:
        def result = failing.orElse(generator)

        then:
        result.isFailing()
        result.unwrapUnsafe().getError().is(error2)
    }

    def "isPassingAnd returns false without calling predicate"() {
        given:
        def error = new RuntimeException("error")
        def failing = new Failing(error)
        def called = false
        def predicate = { String s ->
            called = true
            s.length() > 0
        } as FnPredicateI

        when:
        def result = failing.isPassingAnd(predicate)

        then:
        !result
        !called // Predicate should not be called
    }

    def "unwrapOr returns the default value"() {
        given:
        def error = new RuntimeException("error")
        def failing = new Failing(error)

        when:
        def result = failing.unwrapOr("default")

        then:
        result == "default"
    }

    def "unwrapOrElse calls generator and returns result"() {
        given:
        def error = new RuntimeException("error")
        def failing = new Failing(error)
        def generator = { -> "generated" } as FnGenerateI

        when:
        def result = failing.unwrapOrElse(generator)

        then:
        result == "generated"
    }

    def "unwrapOrElse handles generator exceptions"() {
        given:
        def error = new RuntimeException("original error")
        def failing = new Failing(error)
        def generator = { -> throw new IllegalStateException("generator error") } as FnGenerateI

        when:
        failing.unwrapOrElse(generator)

        then:
        UnwrapException ex = thrown()
        ex.cause instanceof IllegalStateException
        ex.cause.message == "generator error"
    }

    def "passing returns None"() {
        given:
        def error = new RuntimeException("error")
        def failing = new Failing(error)

        when:
        def option = failing.passing()

        then:
        option instanceof None
        option.isNone()
    }

    def "inspect doesn't call function and returns self"() {
        given:
        def error = new RuntimeException("error")
        def failing = new Failing(error)
        def called = false
        def inspector = { String s -> called = true } as FnInspectI

        when:
        def result = failing.inspect(inspector)

        then:
        result.is(failing)
        !called
    }

    def "inspectOrElse calls generator function"() {
        given:
        def error = new RuntimeException("error")
        def failing = new Failing(error)
        def inspectCalled = false
        def inspector = { String s -> inspectCalled = true } as FnInspectI
        def generator = { -> new Passing("fallback") } as FnGenerateI

        when:
        def result = failing.inspectOrElse(inspector, generator)

        then:
        result.isPassing()
        result.unwrap() == "fallback"
        !inspectCalled // Inspect should not be called
    }

    def "chaining operations maintains Failing"() {
        given:
        def error = new RuntimeException("original error")
        def failing = new Failing(error)

        when:
        def result = failing
            .map({ String s -> s.toUpperCase() } as FnTransformI)
            .filter({ String s -> s.length() > 0 } as FnPredicateI)
            .map({ String s -> s.length() } as FnTransformI)

        then:
        result.isFailing()
        result.unwrapUnsafe().getError().is(error)
    }

    def "chaining with or operation provides fallback"() {
        given:
        def error = new RuntimeException("error")
        def failing = new Failing(error)

        when:
        def result = failing
            .map({ String s -> s.toUpperCase() } as FnTransformI)
            .or(new Passing("fallback"))

        then:
        result.isPassing()
        result.unwrap() == "fallback"
    }

    def "thread safety - concurrent access"() {
        given:
        def error = new RuntimeException("shared error")
        def failing = new Failing(error)
        def results = Collections.synchronizedSet([] as Set)

        when:
        (1..100).collect { i ->
            Thread.start {
                def result = failing.map({ String s -> "${s}-${i}" } as FnTransformI)
                results.add(System.identityHashCode(result.unwrapUnsafe().getError()))
            }
        }.each { it.join() }

        then:
        results.size() == 1 // All should reference same error object
        failing.unwrapUnsafe().getError().is(error) // Original unchanged
    }

    def "immutability - all operations return same error"() {
        given:
        def error = new RuntimeException("error")
        def failing = new Failing(error)

        when:
        def mapped = failing.map({ String s -> s } as FnTransformI)
        def filtered = failing.filter({ String s -> true } as FnPredicateI)
        def chained = failing.andThen({ String s -> new Passing(s) } as FnTransformI)
        def inspected = failing.inspect({ String s -> } as FnInspectI)

        then:
        mapped.unwrapUnsafe().getError().is(error)
        filtered.unwrapUnsafe().getError().is(error)
        chained.unwrapUnsafe().getError().is(error)
        inspected.is(failing)
    }

    def "different error types preserved"() {
        given:
        def runtimeError = new RuntimeException("runtime")
        def illegalArgError = new IllegalArgumentException("illegal")
        def nullPointerError = new NullPointerException("null")

        when:
        def runtimeFailing = new Failing(runtimeError)
        def illegalArgFailing = new Failing(illegalArgError)
        def nullPointerFailing = new Failing(nullPointerError)

        then:
        runtimeFailing.unwrapUnsafe().getError() instanceof RuntimeException
        illegalArgFailing.unwrapUnsafe().getError() instanceof IllegalArgumentException
        nullPointerFailing.unwrapUnsafe().getError() instanceof NullPointerException

        runtimeFailing.unwrapUnsafe().getError().message == "runtime"
        illegalArgFailing.unwrapUnsafe().getError().message == "illegal"
        nullPointerFailing.unwrapUnsafe().getError().message == "null"
    }

    def "error message preservation through operations"() {
        given:
        def originalMessage = "original error message"
        def error = new RuntimeException(originalMessage)
        def failing = new Failing(error)

        when:
        def result = failing
            .map({ String s -> s.length() } as FnTransformI)
            .filter({ Integer i -> i > 0 } as FnPredicateI)
            .andThen({ Integer i -> new Passing(i.toString()) } as FnTransformI)

        then:
        result.isFailing()
        result.unwrapUnsafe().getError().message == originalMessage
        result.unwrapUnsafe().getError().is(error)
    }

    def "recovery patterns"() {
        given:
        def error = new RuntimeException("computation failed")
        def failing = new Failing(error)

        when:
        def recovered = failing
            .or(new Passing("default value"))
            .map({ String s -> s.toUpperCase() } as FnTransformI)

        then:
        recovered.isPassing()
        recovered.unwrap() == "DEFAULT VALUE"
    }
}
