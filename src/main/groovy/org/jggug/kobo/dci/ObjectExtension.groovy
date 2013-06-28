package org.jggug.kobo.dci

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

class ObjectExtension {

    // key:targetInstance, value:???
    private static Map<Object, AtomicReference> metaClassRepository = new ConcurrentHashMap().withDefault {
        new AtomicReference<MetaClass>()
    }

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
        def savedMetaClass

        def repository = metaClassRepository.get(self)

        synchronized (repository) {
            assert repository.get() == null: "metaClass of ${self} is already expanded by 'withMixin'"
            savedMetaClass = self.metaClass
            repository.set(savedMetaClass)
        }

        try {
            // replaced a top of metaClass
            def temporaryMetaClass = new DelegatingMetaClass(savedMetaClass)
            self.setMetaClass(temporaryMetaClass)

            // mix-in role temporarily
            self.metaClass.mixin categoryClass

            // evaluate closure
            closure.call()
        } finally {
            self.setMetaClass(repository.getAndSet(null))
        }
    }

    static extendMetaClass() {
        Object.metaClass.mixin ObjectExtension
    }
}
