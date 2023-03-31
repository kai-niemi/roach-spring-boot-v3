package io.roach.spring.statemachine.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.roach.spring.statemachine.domain.Payment;
import io.roach.spring.statemachine.domain.PaymentEvent;
import io.roach.spring.statemachine.domain.PaymentState;
import io.roach.spring.statemachine.repository.PaymentRepository;
import reactor.core.publisher.Mono;

@Service
public class PaymentServiceImpl implements PaymentService {
    public static final String PAYMENT_ID_HEADER = "payment_id";

    public static final String SUCCESS_RATE_HEADER = "random_success_rate";

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;

    @Autowired
    private PaymentStateChangeInterceptor paymentStateChangeInterceptor;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment createPayment(Payment payment) {
        payment.setState(PaymentState.CREATED);
        return paymentRepository.save(payment);
    }

    @Override
    public Optional<Payment> findPayment(Long paymentId) {
        return paymentRepository.findById(paymentId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = load(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.AUTHORIZE);
        return sm;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public StateMachine<PaymentState, PaymentEvent> capturePayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = load(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.CAPTURE);
        return sm;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public StateMachine<PaymentState, PaymentEvent> refundPayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = load(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.REVERSE);
        return sm;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public StateMachine<PaymentState, PaymentEvent> cancelPayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = load(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.CANCEL);
        return sm;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public StateMachine<PaymentState, PaymentEvent> abortPayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = load(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.ABORT);
        return sm;
    }

    private void sendEvent(Long paymentId, StateMachine<PaymentState, PaymentEvent> sm, PaymentEvent event) {
        sm.sendEvent(Mono.just(MessageBuilder.withPayload(event)
                        .setHeader(PAYMENT_ID_HEADER, paymentId)
                        .setHeader(SUCCESS_RATE_HEADER, 100)
                        .build()))
                .subscribe();
//        RuntimeException ex = (RuntimeException) sm.getExtendedState().getVariables().getOrDefault("error", null);
//        if (ex != null) {
//            throw ex;
//        }
    }

    private StateMachine<PaymentState, PaymentEvent> load(Long paymentId) {
        Payment payment = paymentRepository.getReferenceById(paymentId);

        StateMachine<PaymentState, PaymentEvent> sm = stateMachineFactory.getStateMachine(
                Long.toString(payment.getId()));

        sm.stopReactively().block();

        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(paymentStateChangeInterceptor);
                    sma.resetStateMachineReactively(
                                    new DefaultStateMachineContext<>(payment.getState(), null, null, null))
                            .block();
                });

        sm.startReactively().block();

        return sm;
    }
}
