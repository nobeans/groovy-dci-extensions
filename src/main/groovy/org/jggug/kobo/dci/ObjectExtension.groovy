package org.jggug.kobo.dci

class ObjectExtension {

    static Object with(Object self, Class roleClass, Closure closure) {
        def savedMetaClass = self.metaClass
        try {
            // replaced a top of metaClass
            def myMetaClass = new DelegatingMetaClass(savedMetaClass)
            self.setMetaClass(myMetaClass)

            // mix-in role
            self.metaClass.mixin roleClass

            // evaluate closure
            closure.delegate = self
            closure.call()

        } finally {
            // restore metaClass
            self.setMetaClass(savedMetaClass)
        }
    }

    static extendMetaClass() {
        Object.metaClass.mixin ObjectExtension
    }
}
