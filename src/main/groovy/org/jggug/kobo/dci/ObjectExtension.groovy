package org.jggug.kobo.dci

class ObjectExtension {

    static Object mixin(Object self, Class roleClass, Closure closure) {
        def savedMetaClass = self.metaClass
        try {
            // replaced a top of metaClass
            def myMetaClass = new DelegatingMetaClass(savedMetaClass)
            self.setMetaClass(myMetaClass)

            // mix-in role temporarily
            self.metaClass.mixin roleClass

            // evaluate closure
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
