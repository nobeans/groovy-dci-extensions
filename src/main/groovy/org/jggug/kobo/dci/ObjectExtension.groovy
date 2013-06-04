package org.jggug.kobo.dci

class ObjectExtension {

    static Object asRole(Object self, Class roleClass, Closure closure) {
        def savedMetaClass = self.metaClass
        try {
            def myMetaClass = new TemporalDelegatingMetaClass(savedMetaClass)
            self.setMetaClass(myMetaClass)
            self.metaClass.mixin roleClass
            closure.delegate = self
            closure.call()
        } finally {
            self.setMetaClass(savedMetaClass)
        }
    }

    static extendMetaClass() {
        Object.metaClass.mixin ObjectExtension
    }
}
