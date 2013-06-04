package org.jggug.kobo.dci

import org.jggug.kobo.dci.annotations.DciRole
import spock.lang.Specification

class DciRoleSpec extends Specification {

    def setupSpec() {
        ObjectExtension.extendMetaClass()
    }

    def "by `asRole` method to add dynamic methods into only target instance"() {
        given:
        def data = new SampleData(name: "FooBar")

        expect:
        data.asRole(SampleRole) {
            assert data.hello() == "Hello, FooBar."
            return "good"
        } == "good"

        when: "no effect for another instance of SampleData"
        data.asRole(SampleRole) {
            def anotherData = new SampleData(name: "Bazzz")
            anotherData.hello() == "Hello, Bazzz."
            assert false
        }

        then:
        thrown MissingMethodException

        when: "vanish the dynamic method out of the scope"
        data.hello()

        then:
        thrown MissingMethodException
    }

    def "default `use` method looks like good, but it affects all instances of the class. So it cannot be used for DCI."() {
        given:
        def data = new SampleData(name: "FooBar")

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
