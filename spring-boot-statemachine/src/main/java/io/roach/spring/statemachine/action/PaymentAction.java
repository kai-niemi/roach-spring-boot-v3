package io.roach.spring.statemachine.action;

import io.roach.spring.statemachine.domain.PaymentEvent;
import io.roach.spring.statemachine.domain.PaymentState;
import org.springframework.statemachine.action.Action;

public interface PaymentAction extends Action<PaymentState, PaymentEvent> {
}
