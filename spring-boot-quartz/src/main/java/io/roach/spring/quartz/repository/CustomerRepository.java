package io.roach.spring.quartz.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.roach.spring.quartz.domain.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    @Query(value = "select id from customer order by  random() limit 1", nativeQuery = true)
    Optional<UUID> findRandomId();
}
