package io.roach.spring.statemachine.domain;

import java.io.Serializable;

import org.springframework.data.domain.Persistable;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Transient;

@MappedSuperclass
public abstract class AbstractEntity<ID extends Serializable> implements Persistable<ID> {
    @Transient
    private boolean isNew = true;

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}
