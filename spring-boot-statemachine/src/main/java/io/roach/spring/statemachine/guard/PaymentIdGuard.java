package io.roach.spring.statemachine.guard;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;
import org.springframework.stereotype.Component;

import io.roach.spring.statemachine.domain.PaymentEvent;
import io.roach.spring.statemachine.domain.PaymentState;
import io.roach.spring.statemachine.service.PaymentServiceImpl;

@Component
public class PaymentIdGuard implements Guard<PaymentState, PaymentEvent> {
    @Override
    public boolean evaluate(StateContext<PaymentState, PaymentEvent> context) {
        return context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER) != null;
    }
}

