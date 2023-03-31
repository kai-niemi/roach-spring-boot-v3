package io.roach.spring.statemachine.action;

import java.util.Random;

import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.stereotype.Component;

import io.roach.spring.statemachine.domain.PaymentEvent;
import io.roach.spring.statemachine.domain.PaymentState;
import io.roach.spring.statemachine.service.PaymentServiceImpl;

@Component
public class ReverseAction extends AbstractPaymentAction {
    @Override
    public void execute(StateContext<PaymentState, PaymentEvent> context) {
        getLogger().info("Reverse was called!");

        if (new Random().nextInt(10) < 8) {
            getLogger().info("Reverse was successful!");

            context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.REVERSE_SUCCESS)
                    .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
                            context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                    .build());

        } else {
            getLogger().info("Reverse failed!");

            context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.REVERSE_FAILED)
                    .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
                            context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                    .build());
        }
    }
}