package io.roach.spring.timeouts;

import io.roach.spring.timeouts.aop.TransactionAttributesAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AopConfiguration {
    @Bean
    public TransactionAttributesAspect transactionAttributesAspect() {
        return new TransactionAttributesAspect();
    }
}
