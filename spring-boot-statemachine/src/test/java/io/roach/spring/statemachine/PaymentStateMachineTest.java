package io.roach.spring.statemachine;

import java.util.UUID;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

import io.roach.spring.statemachine.domain.PaymentEvent;
import io.roach.spring.statemachine.domain.PaymentState;
import reactor.core.publisher.Mono;

@SpringBootTest
@Tag("interation-test")
public class PaymentStateMachineTest {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;

    @Test
    public void whenStartingStateMachineAndSendingEvents_expectFewStateTransitions() {
        StateMachine<PaymentState, PaymentEvent> sm = stateMachineFactory.getStateMachine(UUID.randomUUID());

        sm.startReactively().subscribe();
        logger.info("State initially: {}", sm.getState().toString());

        sm.sendEvent(Mono.just(MessageBuilder.withPayload(PaymentEvent.AUTHORIZE).build())).subscribe();
        logger.info("State after authorize: {}", sm.getState().toString());

        sm.sendEvent(Mono.just(MessageBuilder.withPayload(PaymentEvent.AUTH_APPROVED).build())).subscribe();
        logger.info("State after auth_approved: {}", sm.getState().toString());

        sm.sendEvent(Mono.just(MessageBuilder.withPayload(PaymentEvent.AUTH_DECLINED).build())).subscribe();
        logger.info("State after auth_declined: {}", sm.getState().toString());
    }
}
