package io.roach.spring.blob.support;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

@MappedSuperclass
public abstract class AbstractPersistentEntity<T extends Serializable> implements PersistentEntity<T> {
    @Transient
    @JsonIgnore
    public boolean isNew() {
        return getId() == null;
    }

    @JsonIgnore
    public abstract T getId();
}
