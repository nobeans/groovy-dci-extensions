package org.jggug.kobo.dci

import spock.lang.Specification

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CountDownLatch

class CategoryUseLearningSpec extends Specification {

    Person person

    def setup() {
        person = new Person(name: "Obama")
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
            use(PresidentRole) {
                waitForStarting("T1")
                try {
                    results << (person.sayHelloTo("Mike") == "Hello, Mike.")
                    assert person.sayHelloTo("Mike") == "Hello, Mike." // to see on stderr
                } finally {
                    waitForEnding("T1")
                }
            }
        }

        and: "and T2 doing this"
        threads << Thread.start {
            use(FatherRole) {
                waitForStarting("T2")
                try {
                    results << (person.sayHelloTo("Michelle") == "Hello, my honey?")
                    assert person.sayHelloTo("Michelle") == "Hello, my honey?" // to see on stderr
                } finally {
                    waitForEnding("T2")
                }
            }
        }

        and:
        threads*.join()

        then: "No problem?"
        results.every { it == true }
        println history // to see
    }

    def "blackdrag: (B) Stack context"() {
        given:
        def foo = { person ->
            assert person.sayHelloTo(person) == "Hello me"
        }

        when:
        use(PresidentRole) {
            assert person.sayHelloTo("Mike") == "Hello, Mike."
            foo(person)
        }

        then: "MissingMethodException occurs in foo!!"
        thrown MissingMethodException
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
