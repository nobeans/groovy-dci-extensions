package org.jggug.kobo.dci

class ObjectExtension {

    /**
     * An alias method of {@code withMixin} for DCI.
     *
     * @param self
     * @param categoryClass
     * @param closure
     * @return
     */
    static Object asRole(Object self, Class categoryClass, Closure closure) {
        withMixin(self, categoryClass, closure)
    }

    /**
     * Mix-in the category to self object only in the scope scope.
     *
     * TODO I want this method as a standard Groovy API!
     *
     * @param self
     * @param categoryClass
     * @param closure
     * @return
     */
    static Object withMixin(Object self, Class categoryClass, Closure closure) {
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
