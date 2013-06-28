package org.jggug.kobo.dci

import spock.lang.Specification

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CountDownLatch

class BlackdragSpec extends Specification {

    Person person

    def setupSpec() {
        ObjectExtension.extendMetaClass()
    }

    def setup() {
        person = new Person(name: "Obama")
    }

    def "nobeans: my sample"() {
        expect:
        person.withMixin(PresidentRole) {
            person.sayHelloTo("Mike") == "Hello, Mike."
        }

        and:
        person.withMixin(FatherRole) {
            person.sayHelloTo("Michelle") == "Hello, my honey?"
        }

        when:
        person.sayHelloTo("Ooops")

        then:
        thrown MissingMethodException
    }

    def "blackdrag: (A) Multithread Context"() {
        given:
        def history = new ArrayBlockingQueue(10)
        def startLatch = new CountDownLatch(2)
        def endLatch = new CountDownLatch(2)
        def waitForStarting = { id ->
            history << "${id}:waiting start"
            startLatch.countDown()
            startLatch.await()
            history << "${id}:started"
        }
        def waitForEnding = { id ->
            history << "${id}:waiting end"
            endLatch.countDown()
            startLatch.await()
            history << "${id}:ended"
        }
        def threads = []
        def results = new ArrayBlockingQueue(2)

        when: "T1 doing this"
        threads << Thread.start {
            person.withMixin(PresidentRole) {
                waitForStarting("T1")
                try {
                    results << (person.sayHelloTo("Mike") == "Hello, Mike.")
                    assert person.sayHelloTo("Mike") == "Hello, Mike."
                } finally {
                    waitForEnding("T1")
                }
            }
        }

        and: "and T2 doing this"
        threads << Thread.start {
            person.withMixin(FatherRole) {
                waitForStarting("T2")
                try {
                    results << (person.sayHelloTo("Michelle") == "Hello, my honey?")
                    assert person.sayHelloTo("Michelle") == "Hello, my honey?"
                } finally {
                    waitForEnding("T2")
                }
            }
        }

        and:
        threads*.join()

        then: "Unfortunately, one is success but another one is failed!"
        results as Set == [true, false] as Set
        println history // to see
    }

    static class Person {
        String name
    }

    @Category(Person)
    static class FatherRole {
        String sayHelloTo(String name) {
            "Hello, my honey?"
        }
    }

    @Category(Person)
    static class PresidentRole {
        String sayHelloTo(String name) {
            "Hello, ${name}."
        }
    }
}
