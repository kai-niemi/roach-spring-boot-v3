package io.roach.spring.statemachine;

import java.math.BigDecimal;
import java.util.EnumSet;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.statemachine.StateMachine;

import io.roach.spring.statemachine.domain.Payment;
import io.roach.spring.statemachine.domain.PaymentEvent;
import io.roach.spring.statemachine.domain.PaymentState;
import io.roach.spring.statemachine.service.PaymentService;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("interation-test")
public class PaymentServiceTest {
    @Autowired
    public PaymentService paymentService;

    private Payment payment;

    @BeforeAll
    public void setUpOnceBeforeTest() {
        this.payment = Payment.builder()
                .withAmount(new BigDecimal("15.49"))
                .withMerchant("Amazon")
                .build();
    }

    @RepeatedTest(10)
    @Order(1)
    public void whenAuthorizePayment_expectAuthorizedOrRejectedState() {
        Payment payment = paymentService.createPayment(this.payment);

        Assertions.assertEquals(PaymentState.CREATED, payment.getState());

        StateMachine<PaymentState, PaymentEvent> sm = paymentService.authorizePayment(payment.getId());

        Assertions.assertTrue(EnumSet.of(PaymentState.AUTH_ERROR, PaymentState.AUTHORIZED)
                .contains(sm.getState().getId()));

        Payment authedPayment = paymentService.findPayment(payment.getId())
                .orElseThrow(() -> new ObjectRetrievalFailureException(Payment.class, payment.getId()));

        Assertions.assertTrue(EnumSet.of(PaymentState.AUTH_ERROR, PaymentState.AUTHORIZED)
                .contains(authedPayment.getState()));
    }

    @Test
    @Order(2)
    public void whenCapturePayment_expectCapturedOrRejectedState() {
        Assertions.assertNotNull(payment);

        Payment persistedPayment = paymentService.findPayment(payment.getId())
                .orElseThrow(() -> new ObjectRetrievalFailureException(Payment.class, payment.getId()));

        Assertions.assertEquals(PaymentState.AUTHORIZED, persistedPayment.getState());

        StateMachine<PaymentState, PaymentEvent> sm = paymentService.capturePayment(persistedPayment.getId());

        Assertions.assertTrue(EnumSet.of(PaymentState.CAPTURE_ERROR, PaymentState.CAPTURED)
                .contains(sm.getState().getId()));

        Payment capturedPayment = paymentService.findPayment(persistedPayment.getId())
                .orElseThrow(() -> new ObjectRetrievalFailureException(Payment.class, persistedPayment.getId()));

        Assertions.assertTrue(EnumSet.of(PaymentState.CAPTURE_ERROR, PaymentState.CAPTURED)
                .contains(capturedPayment.getState()));
    }

    @Test
    @Order(3)
    public void whenRefundPayment_expectReversedOrRejectedState() {
        Assertions.assertNotNull(payment);

        Payment persistedPayment = paymentService.findPayment(payment.getId())
                .orElseThrow(() -> new ObjectRetrievalFailureException(Payment.class, payment.getId()));

        Assertions.assertEquals(PaymentState.CAPTURED, persistedPayment.getState());

        StateMachine<PaymentState, PaymentEvent> sm = paymentService.refundPayment(persistedPayment.getId());

        Assertions.assertTrue(EnumSet.of(PaymentState.REVERSED, PaymentState.REVERSE_ERROR)
                .contains(sm.getState().getId()));

        Payment refundedPayment = paymentService.findPayment(persistedPayment.getId())
                .orElseThrow(() -> new ObjectRetrievalFailureException(Payment.class, persistedPayment.getId()));

        Assertions.assertTrue(EnumSet.of(PaymentState.REVERSE_ERROR, PaymentState.REVERSED)
                .contains(refundedPayment.getState()));
    }

    @Test
    @Order(4)
    public void whenCancelAuthorizedPayment_expectCancelledState() {
        Payment temporalPayment = paymentService.createPayment(Payment.builder()
                .withAmount(new BigDecimal("5.59"))
                .withMerchant("Ebay")
                .build());

        Assertions.assertEquals(PaymentState.CREATED, temporalPayment.getState());

        StateMachine<PaymentState, PaymentEvent> sm = paymentService.authorizePayment(temporalPayment.getId());

        Assertions.assertTrue(EnumSet.of(PaymentState.AUTHORIZED).contains(sm.getState().getId()));

        sm = paymentService.cancelPayment(temporalPayment.getId());

        Assertions.assertTrue(EnumSet.of(PaymentState.CANCELLED)
                .contains(sm.getState().getId()));

        Payment cancelledPayment = paymentService.findPayment(temporalPayment.getId())
                .orElseThrow(() -> new ObjectRetrievalFailureException(Payment.class, temporalPayment.getId()));

        Assertions.assertTrue(EnumSet.of(PaymentState.CANCELLED)
                .contains(cancelledPayment.getState()));
    }

    @Test
    @Order(5)
    public void whenAbortNewPayment_expectAbortedState() {
        Payment temporalPayment = paymentService.createPayment(Payment.builder()
                .withAmount(new BigDecimal("5.59"))
                .withMerchant("Ebay")
                .build());

        Assertions.assertEquals(PaymentState.CREATED, temporalPayment.getState());

        StateMachine<PaymentState, PaymentEvent> sm = paymentService.abortPayment(temporalPayment.getId());

        Assertions.assertTrue(EnumSet.of(PaymentState.ABORTED).contains(sm.getState().getId()));

        Payment abortedPayment = paymentService.findPayment(temporalPayment.getId())
                .orElseThrow(() -> new ObjectRetrievalFailureException(Payment.class, temporalPayment.getId()));

        Assertions.assertTrue(EnumSet.of(PaymentState.ABORTED)
                .contains(abortedPayment.getState()));
    }
}