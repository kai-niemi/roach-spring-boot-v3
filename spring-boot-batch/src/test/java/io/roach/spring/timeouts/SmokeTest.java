package io.roach.spring.timeouts;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.roach.spring.timeouts.domain.Order;
import io.roach.spring.timeouts.domain.OrderItem;
import io.roach.spring.timeouts.domain.OrderService;
import io.roach.spring.timeouts.domain.Product;
import io.roach.spring.timeouts.domain.ShipmentStatus;

public class SmokeTest extends AbstractIntegrationTest {
    @Autowired
    protected OrderService orderService;

    @BeforeAll
    public void setupTest() {
        testDoubles.deleteTestDoubles();
        testDoubles.createProducts(100, product -> {
        });
        testDoubles.createCustomers(10);
    }

    @org.junit.jupiter.api.Order(1)
    @Test
    public void whenCreatingSingletonOrders_thenSucceed() {
        testDoubles.newOrders(10, 4, order -> orderService.placeOrder(order));
    }

    @org.junit.jupiter.api.Order(2)
    @Test
    public void whenReadingOrders_thenSucceed() {
        List<Order> orders = orderService.findOrdersByStatus(ShipmentStatus.placed, 1);
        Assertions.assertEquals(1, orders.size());

        orders.forEach(order -> {
            List<OrderItem> items = order.getOrderItems();

            Assertions.assertEquals(4, items.size());

            items.forEach(orderItem -> {
                Product product = orderItem.getProduct();
                logger.info("{}", product.toString());
                Assertions.assertTrue(orderItem.getQuantity() > 0);
            });
        });
    }
}
