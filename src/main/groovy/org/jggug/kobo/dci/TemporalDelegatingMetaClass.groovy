package org.jggug.kobo.dci

class TemporalDelegatingMetaClass extends DelegatingMetaClass {

    TemporalDelegatingMetaClass(MetaClass metaClass) {
        super(metaClass)
        initialize()
    }
}
