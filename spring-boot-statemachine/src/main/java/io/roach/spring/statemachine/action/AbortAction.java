package io.roach.spring.statemachine.action;

import org.springframework.statemachine.StateContext;
import org.springframework.stereotype.Component;

import io.roach.spring.statemachine.domain.PaymentEvent;
import io.roach.spring.statemachine.domain.PaymentState;

@Component
public class AbortAction extends AbstractPaymentAction {
    @Override
    public void execute(StateContext<PaymentState, PaymentEvent> context) {
        getLogger().info("Abort called!");
    }
}
