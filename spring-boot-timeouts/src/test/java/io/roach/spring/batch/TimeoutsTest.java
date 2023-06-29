package io.roach.spring.batch;

import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import io.roach.spring.timeouts.domain.Order;
import io.roach.spring.timeouts.domain.OrderRepository;
import io.roach.spring.timeouts.domain.OrderService;
import io.roach.spring.timeouts.domain.Product;
import io.roach.spring.timeouts.domain.ProductRepository;

public class TimeoutsTest extends AbstractIntegrationTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeAll
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setupTest() {
        Assertions.assertTrue(
                TransactionSynchronizationManager.isActualTransactionActive(), "Explicit transaction required");

        orderRepository.deleteAllOrderItems();
        orderRepository.deleteAll();
        productRepository.deleteAll();

        productRepository.save(Product.builder()
                .withName("p1")
                .withPrice(BigDecimal.ONE)
                .withSku("p1")
                .withQuantity(50)
                .build());
    }

    @org.junit.jupiter.api.Order(1)
    @Test
    public void whenCreatingOrderWithTimeout_thenExpectRollbackError() {
        Product p1 = orderService.findProduct("p1");

        orderService.placeOrderWithTimeout(Order.builder()
                .andOrderItem()
                .withProduct(p1)
                .withQuantity(1)
                .withUnitPrice(p1.getPrice())
                .then()
                .build());
    }

    @org.junit.jupiter.api.Order(1)
    @Test
    public void whenCreatingOrderWithoutTimeout_thenExpectCommit() {
        Product p1 = orderService.findProduct("p1");

        orderService.placeOrderWithTimeout(Order.builder()
                .andOrderItem()
                .withProduct(p1)
                .withQuantity(1)
                .withUnitPrice(p1.getPrice())
                .then()
                .build());
    }
}
