package io.roach.spring.statemachine.aspect;

import org.springframework.core.Ordered;

public abstract class AdvisorOrder {
    private AdvisorOrder() {
    }

    public static final int TRANSACTION_RETRY_ADVISOR = Ordered.LOWEST_PRECEDENCE - 4;

    public static final int TRANSACTION_ADVISOR = Ordered.LOWEST_PRECEDENCE - 3;
}
