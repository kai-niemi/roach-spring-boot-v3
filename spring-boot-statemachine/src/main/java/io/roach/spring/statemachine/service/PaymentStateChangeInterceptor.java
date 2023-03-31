package io.roach.spring.statemachine.service;

import java.sql.SQLException;
import java.util.Optional;

import org.postgresql.util.PSQLState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import io.roach.spring.statemachine.domain.Payment;
import io.roach.spring.statemachine.domain.PaymentEvent;
import io.roach.spring.statemachine.domain.PaymentState;
import io.roach.spring.statemachine.repository.PaymentRepository;

@Component
public class PaymentStateChangeInterceptor extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {
    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public void preStateChange(State<PaymentState, PaymentEvent> state, Message<PaymentEvent> message,
                               Transition<PaymentState, PaymentEvent> transition,
                               StateMachine<PaymentState, PaymentEvent> stateMachine,
                               StateMachine<PaymentState, PaymentEvent> rootStateMachine) {
        super.preStateChange(state, message, transition, stateMachine, rootStateMachine);

        Optional.ofNullable(message).ifPresent(msg -> {
            Long paymentId = Long.class.cast(
                    msg.getHeaders().getOrDefault(PaymentServiceImpl.PAYMENT_ID_HEADER, -1L));
            if (paymentId != null) {
                Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "No transaction context!");

                Payment payment = paymentRepository.getReferenceById(paymentId);
                payment.setState(state.getId());

                paymentRepository.save(payment);

                ConcurrencyFailureException ex = new ConcurrencyFailureException("Fake SQL error",
                        new SQLException("Serialization conflict!", PSQLState.SERIALIZATION_FAILURE.getState(), 0));
                stateMachine.getExtendedState().getVariables().put("error", ex);
                throw ex;
            }
        });
    }

    @Override
    public Exception stateMachineError(StateMachine<PaymentState, PaymentEvent> stateMachine, Exception exception) {
        return super.stateMachineError(stateMachine, exception);
    }
}
