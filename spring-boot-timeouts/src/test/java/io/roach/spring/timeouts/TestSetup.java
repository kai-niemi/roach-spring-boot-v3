package io.roach.spring.timeouts;

import io.roach.spring.timeouts.domain.OrderRepository;
import io.roach.spring.timeouts.domain.Product;
import io.roach.spring.timeouts.domain.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import java.math.BigDecimal;

@Service
public class TestSetup {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setupTestData() {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "TX not active");

        orderRepository.deleteAllOrderItems();
        orderRepository.deleteAll();
        orderRepository.flush();

        productRepository.deleteAll();
        productRepository.flush();

        productRepository.save(Product.builder()
                .withName("p1")
                .withPrice(BigDecimal.ONE)
                .withSku("p1")
                .withQuantity(50)
                .build());

        productRepository.save(Product.builder()
                .withName("p2")
                .withPrice(BigDecimal.ONE)
                .withSku("p2")
                .withQuantity(150)
                .build());
    }
}
