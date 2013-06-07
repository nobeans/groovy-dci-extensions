package org.jggug.kobo.dci

class ObjectExtension {

    static void mixin(Object self, Class categoryClass) {
        self.metaClass.mixin categoryClass
    }

    static Object mixin(Object self, Class categoryClass, Closure closure) {
        def savedMetaClass = self.metaClass
        try {
            // replaced a top of metaClass
            def temporaryMetaClass = new DelegatingMetaClass(savedMetaClass)
            self.setMetaClass(temporaryMetaClass)

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
