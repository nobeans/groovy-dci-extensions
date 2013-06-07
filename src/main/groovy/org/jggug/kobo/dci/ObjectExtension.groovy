package org.jggug.kobo.dci

class ObjectExtension {

    static Object mixin(Object self, Class categoryClass) {
        self.metaClass.mixin categoryClass
    }

    static Object mixin(Object self, Class categoryClass, Closure closure) {
        def savedMetaClass = self.metaClass
        try {
            // replaced a top of metaClass
            def myMetaClass = new DelegatingMetaClass(savedMetaClass)
            self.setMetaClass(myMetaClass)

            // mix-in role temporarily
            self.metaClass.mixin categoryClass

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
