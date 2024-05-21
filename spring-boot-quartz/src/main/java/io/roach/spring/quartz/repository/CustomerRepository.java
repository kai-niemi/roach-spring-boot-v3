package io.roach.spring.quartz.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.roach.spring.quartz.domain.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
}
