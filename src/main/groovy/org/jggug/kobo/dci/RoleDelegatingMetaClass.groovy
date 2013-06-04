package org.jggug.kobo.dci

class RoleDelegatingMetaClass extends DelegatingMetaClass {

    RoleDelegatingMetaClass(MetaClass metaClass) {
        super(metaClass)
        initialize()
    }
}
