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

    def "by `with` method to add dynamic methods into target instance"() {
        expect:
        data.with(SampleRole) {
            assert data.hello() == "Hello, FooBar."
            return "good"
        } == "good"
    }

    def "delegate to the data object if missing the method because it's `with` method"() {
        expect:
        data.with(SampleRole) { hello() == "Hello, FooBar." }
    }

    def "vanish the dynamic method after calling `with` method"() {
        when:
        data.hello()

        then:
        thrown MissingMethodException
    }

    def "by `with` method to add dynamic methods into only target instance"() {
        when: "no effect for another instance of SampleData"
        data.with(SampleRole) {
            def anotherData = new SampleData(name: "Bazzz")
            anotherData.hello() == "Hello, Bazzz."
            assert false
        }

        then:
        thrown MissingMethodException
    }

    def "The original `with(Closure)` is still avaiable"() {
        expect:
        data.with { name == "FooBar" }

        when: "of course, there isn't `hello()` method"
        data.hello()

        then:
        thrown MissingMethodException
    }

    def "default `use` method looks like good, but it affects all instances of the class. So it cannot be used for DCI."() {
        expect:
        data.use(SampleRole) {
            assert data.hello() == "Hello, FooBar."
            return "good"
        } == "good"

        and: "Unfortunately, `use` method affects another instance of SampleData"
        data.use(SampleRole) {
            def anotherData = new SampleData(name: "Bazzz")
            assert anotherData.hello() == "Hello, Bazzz."
            return "too bad"
        } == "too bad"

        when: "vanish the dynamic method out of the scope"
        data.hello()

        then:
        thrown MissingMethodException
    }

    static class SampleData {
        String name
    }

    @DciRole(SampleData)
    static class SampleRole {
        String hello() {
            "Hello, ${this.name}."
        }
    }
}
