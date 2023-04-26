package io.roach.spring.blob.support;

import java.io.Serializable;

public interface PersistentEntity<T extends Serializable> extends Serializable {
    boolean isNew();
}
