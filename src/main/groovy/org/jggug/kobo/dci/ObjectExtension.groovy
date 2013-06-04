package org.jggug.kobo.dci

class ObjectExtension {

    static setup() {
        SampleData.metaClass.as = { Class klass, Closure closure ->
            // TODO ここで一時的にdelegateのmetaClasssにklassを追加したい
//            delegate.metaClass.mixin klass
//            closure.delegate = delegate
//            closure.call()

            def savedMetaClass = delegate.metaClass
            try {
                def myMetaClass = new MyDelegatingMetaClass(savedMetaClass)
                delegate.setMetaClass(myMetaClass)
                delegate.metaClass.mixin klass
                closure.delegate = delegate
                closure.call()
            } finally {
                delegate.setMetaClass(savedMetaClass)
            }
        }
    }
}
