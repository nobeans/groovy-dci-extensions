package org.jggug.kobo.dci

import spock.lang.Specification

class DciRoleSpec extends Specification {

    SampleData data

    def setupSpec() {
        ObjectExtension.extendMetaClass()
    }

    def setup() {
        data = new SampleData(name: "FooBar")
    }

    def "the name of `asRole` is appropriate for DCI"() {
        expect:
        data.asRole(Greeter) {
            assert data.hello() == "Hello, FooBar."
            return "good"
        } == "good"

        // but in the point of view of a standard API of Groovy, withMixin is better.
        // "Role" is a word of DCI.
    }

    def "by `withMixin` method to add dynamic methods into target instance"() {
        expect:
        data.withMixin(Greeter) {
            assert data.hello() == "Hello, FooBar."
            return "good"
        } == "good"
    }

    def "vanish the dynamic method after calling `withMixin` method"() {
        given:
        data.withMixin(Greeter) { data.hello() }

        when:
        data.hello()

        then:
        thrown MissingMethodException
    }

    def "by `withMixin` method to add dynamic methods into only target instance"() {
        when: "no effect for another instance of SampleData"
        data.withMixin(Greeter) {
            def anotherData = new SampleData(name: "Bazzz")
            anotherData.hello() == "Hello, Bazzz."
            assert false
        }

        then:
        thrown MissingMethodException
    }

    def "default `use` method looks like good, but it affects all instances of the class. So it cannot be used for DCI."() {
        expect:
        data.use(Greeter) {
            assert data.hello() == "Hello, FooBar."
            return "good"
        } == "good"

        and: "Unfortunately, `use` method affects another instance of SampleData."
        // Type of receiver has no meaning.
        // All instances of the class are affected by the 'use' method in the scope of the closure.
        data.use(Greeter) {
            def anotherData = new SampleData(name: "Bazzz")
            assert anotherData.hello() == "Hello, Bazzz."
            return "too bad"
        } == "too bad"

        when: "vanish the dynamic method out of the scope"
        data.hello()

        then:
        thrown MissingMethodException
    }

    def "default `use` method doesn't affect another thread."() {
        expect:
        use(Greeter) {
            Thread.start {
                def anotherThreadData = new SampleData(name: "Bazzz")
                try {
                    anotherThreadData.hello()
                    assert false
                } catch (MissingMethodException e) {
                    // OK
                }
            }.join()

            assert data.hello() == "Hello, FooBar."
            return "good"

        } == "good"
    }

    def "`mixin` affects an instance permanently among threads."() {
        when:
        Thread.start {
            data.metaClass.mixin Greeter
            assert data.hello() == "Hello, FooBar."
        }.join()

        then: "mixin effect is still there"
        assert data.hello() == "Hello, FooBar."

        and:
        Thread.start {
            assert data.hello() == "Hello, FooBar."
        }.join()
    }

    def "blagdrag(A): `withMixin` affects an instance permanently among threads."() {
        when:
        Thread.start {
            data.metaClass.mixin Greeter
            assert data.hello() == "Hello, FooBar."
        }.join()
        Thread.start {
            data.metaClass.mixin Greeter
            assert data.hello() == "Hello, FooBar."
        }.join()

        then: "mixin effect is still there"
        assert data.hello() == "Hello, FooBar."

        and:
        Thread.start {
            assert data.hello() == "Hello, FooBar."
        }.join()
    }


    static class SampleData {
        String name
    }

    @DciRole(SampleData)
    static class Greeter {
        String hello() {
            "Hello, ${this.name}."
        }
    }
}
