package io.roach.spring.batch;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import io.roach.spring.batch.domain.Order;
import io.roach.spring.batch.domain.OrderService;
import io.roach.spring.batch.util.Timer;

//@ActiveProfiles
public class BenchmarkTest extends AbstractIntegrationTest {
    protected final int numProducts = 250;

    protected final int numCustomers = 1000;

    @Autowired
    protected OrderService orderService;

    @BeforeAll
    public void setupTest() {
        Assertions.assertFalse(TransactionSynchronizationManager.isActualTransactionActive(), "TX active");

        testDoubles.deleteTestDoubles();
        testDoubles.createProducts(numProducts, product -> {
        });
        testDoubles.createCustomers(numCustomers);
    }

    @org.junit.jupiter.api.Order(1)
    @ParameterizedTest
    @ValueSource(ints = {500, 1000, 1500, 2000, 2500, 3000, 3500, 4000})
    public void whenCreatingBulkOrders_thenAlsoUpdateInventory(int numOrders) {
        final List<Order> orders = new ArrayList<>();
        testDoubles.newOrders(numOrders, 4, orders::add);
        Timer.timeExecution("placeOrderAndUpdateInventory(" + numOrders + ")",
                () -> orderService.placeOrderAndUpdateInventory(orders));
    }
}
