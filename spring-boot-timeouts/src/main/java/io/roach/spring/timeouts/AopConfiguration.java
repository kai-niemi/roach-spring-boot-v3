package io.roach.spring.timeouts;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.roach.spring.timeouts.aop.TransactionAttributesAspect;

@Configuration
public class AopConfiguration {
    @Bean
    public TransactionAttributesAspect transactionHintsAspect() {
        return new TransactionAttributesAspect();
    }
}
