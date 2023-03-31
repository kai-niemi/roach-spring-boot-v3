package io.roach.spring.statemachine.action;

import java.lang.reflect.UndeclaredThrowableException;

import org.springframework.statemachine.StateContext;
import org.springframework.stereotype.Component;

import io.roach.spring.statemachine.domain.PaymentEvent;
import io.roach.spring.statemachine.domain.PaymentState;

@Component
public class ErrorAction extends AbstractPaymentAction {
    @Override
    public void execute(StateContext<PaymentState, PaymentEvent> context) {
        Exception ex = context.getException();
//        Throwable throwable = NestedExceptionUtils.getMostSpecificCause(ex);
        throw new UndeclaredThrowableException(ex);
    }
}
