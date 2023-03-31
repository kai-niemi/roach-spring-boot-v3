package io.roach.spring.statemachine;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;

import io.roach.spring.statemachine.aspect.TransactionRetryAspect;

@Configuration
@EnableAspectJAutoProxy
public class AopConfig {
    @Bean
    @Profile("retry-client")
    public TransactionRetryAspect transactionRetryAspect() {
        return new TransactionRetryAspect();
    }
}
