package io.roach.spring.timeouts;

import io.roach.spring.timeouts.domain.Order;
import io.roach.spring.timeouts.domain.OrderService;
import io.roach.spring.timeouts.domain.Product;
import org.hibernate.TransactionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TimeoutsTest extends AbstractIntegrationTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private TestSetup testSetup;

    @BeforeAll
    public void setupTest() {
        testSetup.setupTestData();
    }

    @org.junit.jupiter.api.Order(1)
    @Test
    public void whenCreatingOrderWithTimeout_thenExpectRollbackError() {
        Product p1 = orderService.findProduct("p1");

        TransactionException ex = Assertions.assertThrows(TransactionException.class, () -> {
            orderService.placeOrderWithTimeout(Order.builder()
                    .andOrderItem()
                    .withProduct(p1)
                    .withQuantity(1)
                    .withUnitPrice(p1.getPrice())
                    .then()
                    .build());
        });

        logger.info("Exception thrown", ex);
        Assertions.assertEquals("transaction timeout expired", ex.getMessage());
    }

    @org.junit.jupiter.api.Order(1)
    @Test
    public void whenCreatingOrderWithoutTimeout_thenExpectCommit() {
        Product p1 = orderService.findProduct("p1");

        orderService.placeOrderWithoutTimeout(Order.builder()
                .andOrderItem()
                .withProduct(p1)
                .withQuantity(1)
                .withUnitPrice(p1.getPrice())
                .then()
                .build());
    }
}
