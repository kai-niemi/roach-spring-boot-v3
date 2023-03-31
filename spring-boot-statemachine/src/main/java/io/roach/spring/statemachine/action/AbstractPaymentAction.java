package io.roach.spring.statemachine.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPaymentAction implements PaymentAction {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public Logger getLogger() {
        return logger;
    }
}
