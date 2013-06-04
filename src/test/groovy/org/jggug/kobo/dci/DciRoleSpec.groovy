package org.jggug.kobo.dci

import org.jggug.kobo.dci.annotations.DciRole
import spock.lang.Specification

class DciRoleSpec extends Specification {

    def "default `use` method looks like good, but it affects all instances of the class"() {
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

    def "by `as` method to add dynamic methods into only target instance"() {
        given:
        SampleData.metaClass.as = { Class klass, Closure closure ->
            // TODO ここで一時的にdelegateのmetaClasssにklassを追加したい
//            delegate.metaClass.mixin klass
//            closure.delegate = delegate
//            closure.call()

            def savedMetaClass = delegate.metaClass
            try {
                def myMetaClass = new MyDelegatingMetaClass(savedMetaClass)
                delegate.setMetaClass(myMetaClass)
                delegate.metaClass.mixin klass
                closure.delegate = delegate
                closure.call()
            } finally {
                delegate.setMetaClass(savedMetaClass)
            }
        }

        and:
        def data = new SampleData(name: "FooBar")

        expect:
        data.as(SampleRole) {
            assert data.hello() == "Hello, FooBar."
        } == null

        when: "for another instance of SampleData"
        data.as(SampleRole) {
            assert data.hello() == "Hello, FooBar."

            // 無念
            def data2 = new SampleData(name: "Bazzz")
            data2.hello() == "Hello, Bazzz."

        } == null

        then:
        thrown MissingMethodException

        when: "vanish the dynamic method out of the scope"
        data.hello()

        then:
        thrown MissingMethodException
    }
//        and: "言語仕様のasの意味を汚染するほどの価値はなさげ..."
//        (data as SampleRole) {
//            assert data.hello() == "Hello, FooBar."
//        } == null
//
//        and: "言語仕様のasの意味を汚染するほどの価値はなさげ..."
//        data.as(SampleRole) {
//            assert data.hello() == "Hello, FooBar."
//        } == null

//        and:
//        data.asRole(SampleRole) {
//            assert data.hello() == "Hello, FooBar."
//        } == null
//
//        and:
//        data.withRole(SampleRole) {
//            assert data.hello() == "Hello, FooBar."
//        } == null

    static class SampleData {
        String name
    }

    @DciRole(SampleData)
    static class SampleRole {
        String hello() {
            "Hello, ${this.name}."
        }
    }

    static class MyDelegatingMetaClass extends DelegatingMetaClass {
        MyDelegatingMetaClass(MetaClass metaClass) {
            super(metaClass);
            initialize()
        }

        public Object invokeMethod(Object a_object, String a_methodName, Object[] a_arguments) {
            return "changed ${super.invokeMethod(a_object, a_methodName, a_arguments)}"
        }
    }
}
