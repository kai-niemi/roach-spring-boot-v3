package io.roach.spring.timeouts;

import io.roach.spring.timeouts.domain.Order;
import io.roach.spring.timeouts.domain.OrderService;
import io.roach.spring.timeouts.domain.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaSystemException;

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
    public void whenCreatingOrderWithTimeoutThatExpires_thenExpectRollback() {
        Product p1 = orderService.findProduct("p1");
        int inventory = p1.getInventory();

        JpaSystemException ex = Assertions.assertThrows(JpaSystemException.class, () -> {
            orderService.placeOrderWithTimeout(Order.builder()
                            .andOrderItem()
                            .withProduct(p1)
                            .withQuantity(1)
                            .withUnitPrice(p1.getPrice())
                            .then()
                            .build(),
                    7000);
        });

        Assertions.assertEquals("transaction timeout expired", ex.getMessage());
        Assertions.assertEquals(inventory, orderService.findProduct("p1").getInventory());

        logger.info("Exception thrown", ex);
    }

    @org.junit.jupiter.api.Order(2)
    @Test
    public void whenCreatingOrderWithTimeout_thenExpectCommit() {
        Product p1 = orderService.findProduct("p1");
        int inventory = p1.getInventory();

        orderService.placeOrderWithTimeout(Order.builder()
                        .andOrderItem()
                        .withProduct(p1)
                        .withQuantity(1)
                        .withUnitPrice(p1.getPrice())
                        .then()
                        .build(),
                2000);

        Assertions.assertEquals(inventory - 1, orderService.findProduct("p1").getInventory());
    }

    @org.junit.jupiter.api.Order(3)
    @Test
    public void whenCreatingOrderWithoutTimeout_thenExpectCommit() {
        Product p1 = orderService.findProduct("p1");
        int inventory = p1.getInventory();

        orderService.placeOrderWithoutTimeout(Order.builder()
                .andOrderItem()
                .withProduct(p1)
                .withQuantity(1)
                .withUnitPrice(p1.getPrice())
                .then()
                .build());

        Assertions.assertEquals(inventory - 1, orderService.findProduct("p1").getInventory());
    }
}
