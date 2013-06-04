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
        } == null

        when: "for another instance of SampleData"
        data.asRole(SampleRole) {
            assert data.hello() == "Hello, FooBar."

            // no effect: data2 doesn't have hello method
            def data2 = new SampleData(name: "Bazzz")
            data2.hello() == "Hello, Bazzz."
            assert false

        } == null

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
        } == null

        and: "Unfortunately, `use` affects another instance of SampleData"
        data.use(SampleRole) {
            assert data.hello() == "Hello, FooBar."

            // 影響が漏れている。data2はSampleRoleのつもりじゃないのに！
            def data2 = new SampleData(name: "Bazzz")
            assert data2.hello() == "Hello, Bazzz."

        } == null

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
