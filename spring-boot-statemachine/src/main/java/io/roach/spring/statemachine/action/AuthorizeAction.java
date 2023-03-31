package io.roach.spring.statemachine.action;

import java.util.Optional;

import io.roach.spring.statemachine.domain.PaymentEvent;
import io.roach.spring.statemachine.domain.PaymentState;
import io.roach.spring.statemachine.service.PaymentServiceImpl;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.stereotype.Component;

import io.roach.spring.statemachine.util.Randomizer;

@Component
public class AuthorizeAction extends AbstractPaymentAction {
    @Override
    public void execute(StateContext<PaymentState, PaymentEvent> context) {
        Object paymentId = context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER);

        int randomErrorProbability = (int)
                Optional.ofNullable(context.getMessageHeader(PaymentServiceImpl.SUCCESS_RATE_HEADER)).orElse(0);

        if (Randomizer.withProbability(() -> true, () -> false, randomErrorProbability)) {
            getLogger().info("Authorize approved! {}", paymentId);

            context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.AUTH_APPROVED)
                    .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
                            context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                    .build());

        } else {
            getLogger().info("Authorize declined! {}", paymentId);

            context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.AUTH_DECLINED)
                    .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
                            context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                    .build());
        }
    }
}
