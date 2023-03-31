package io.roach.spring.statemachine.action;

import java.util.Optional;

import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.stereotype.Component;

import io.roach.spring.statemachine.domain.PaymentEvent;
import io.roach.spring.statemachine.domain.PaymentState;
import io.roach.spring.statemachine.service.PaymentServiceImpl;
import io.roach.spring.statemachine.util.Randomizer;

@Component
public class CaptureAction extends AbstractPaymentAction {
    @Override
    public void execute(StateContext<PaymentState, PaymentEvent> context) {
        getLogger().info("Capture was called!");

        int randomErrorProbability = (int)
                Optional.ofNullable(context.getMessageHeader(PaymentServiceImpl.SUCCESS_RATE_HEADER)).orElse(0);

        if (Randomizer.withProbability(() -> true, () -> false, randomErrorProbability)) {
            getLogger().info("Capture was successful!");

            context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.CAPTURE_SUCCESS)
                    .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
                            context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                    .build());

        } else {
            getLogger().info("Capture failed!");

            context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.CAPTURE_FAILED)
                    .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
                            context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                    .build());
        }
    }
}