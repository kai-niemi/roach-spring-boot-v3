package io.roach.spring.statemachine.service;

import java.util.Optional;

import org.springframework.statemachine.StateMachine;

import io.roach.spring.statemachine.domain.Payment;
import io.roach.spring.statemachine.domain.PaymentEvent;
import io.roach.spring.statemachine.domain.PaymentState;

public interface PaymentService {
    Payment createPayment(Payment payment);

    Optional<Payment> findPayment(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> capturePayment(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> refundPayment(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> cancelPayment(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> abortPayment(Long paymentId);
}
