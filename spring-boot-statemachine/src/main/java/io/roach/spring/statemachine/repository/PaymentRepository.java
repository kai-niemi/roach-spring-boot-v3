package io.roach.spring.statemachine.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.roach.spring.statemachine.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
