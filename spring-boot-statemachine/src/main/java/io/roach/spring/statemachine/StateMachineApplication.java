package io.roach.spring.statemachine;

import io.roach.spring.statemachine.aspect.AdvisorOrder;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement(order = AdvisorOrder.TRANSACTION_ADVISOR)
public class StateMachineApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(StateMachineApplication.class)
                .logStartupInfo(true)
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.CONSOLE)
                .run(args);
    }
}

