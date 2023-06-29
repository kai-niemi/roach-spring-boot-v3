package io.roach.spring.timeouts;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.roach.spring.timeouts.aop.TransactionHintsAspect;

@Configuration
public class AopConfiguration {
    @Bean
    public TransactionHintsAspect transactionHintsAspect() {
        return new TransactionHintsAspect();
    }
}
