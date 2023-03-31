package io.roach.spring.statemachine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.statemachine.StateMachine;

import io.roach.spring.statemachine.domain.Payment;
import io.roach.spring.statemachine.domain.PaymentEvent;
import io.roach.spring.statemachine.domain.PaymentState;
import io.roach.spring.statemachine.service.PaymentService;
import io.roach.spring.statemachine.util.ConcurrencyUtils;

@SpringBootTest
@Tag("stress-test")
public class PaymentServiceStressTest {
    @Autowired
    public PaymentService paymentService;

    @Test
    public void whenAuthorizePayment_expectAuthorizedOrRejectedState() {
        Payment payment = paymentService.createPayment(Payment.builder()
                .withAmount(new BigDecimal("15.49"))
                .withMerchant("Amazon")
                .build());

        Assertions.assertEquals(PaymentState.CREATED, payment.getState());

        List<Callable<Void>> tasks = new ArrayList<>();

        IntStream.range(1, 512).forEach(value -> {
            Callable<Void> c = () -> {
                StateMachine<PaymentState, PaymentEvent> sm = paymentService.authorizePayment(payment.getId());
                Assertions.assertTrue(EnumSet.of(PaymentState.AUTHORIZED).contains(sm.getState().getId()));
                return null;
            };
            tasks.add(c);
        });

        ConcurrencyUtils.runConcurrentlyAndWait(Runtime.getRuntime().availableProcessors() * 4, tasks);

        Payment authedPayment = paymentService.findPayment(payment.getId())
                .orElseThrow(() -> new ObjectRetrievalFailureException(Payment.class, payment.getId()));

        Assertions.assertTrue(EnumSet.of(PaymentState.AUTH_ERROR, PaymentState.AUTHORIZED)
                .contains(authedPayment.getState()));
    }
}
