package org.jggug.kobo.dci

class ObjectExtension {

    static Object asRole(Object self, Class roleClass, Closure closure) {
        println ">" * 50
        println "self.metaClass: " + self.metaClass

        def savedMetaClass = self.metaClass

        println "savedMetaClass: " + savedMetaClass
        try {
            def myMetaClass = new DelegatingMetaClass(self.getClass())

            println "> myMetaClass: " + myMetaClass
            println "> myMetaClass.getMetaMethods: " + myMetaClass.getMetaMethods()*.name.sort()
            println myMetaClass.getMetaMethod("hello", [] as Object[])

            self.setMetaClass(myMetaClass)

            println "> self.metaClass:" + self.metaClass

            self.metaClass.mixin roleClass

            println "> self.metaClass.getMetaMethods: " + self.metaClass.getMetaMethods()*.name.sort()
            println myMetaClass.getMetaMethod("hello", [] as Object[])
            println savedMetaClass.getMetaMethod("hello", [] as Object[])
            println self.metaClass.getMetaMethod("hello", [] as Object[])

            closure.delegate = self
            closure.call()

        } finally {
            println "savedMetaClass: " + savedMetaClass
            self.setMetaClass(savedMetaClass)
            println "self.metaClass: " + self.metaClass
            println self.metaClass.getMetaMethod("hello", [] as Object[])
        }
    }

    static extendMetaClass() {
        Object.metaClass.mixin ObjectExtension
    }
}
