package io.roach.spring.timeouts.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import jakarta.persistence.EntityNotFoundException;

@Service
public class OrderService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Product findProduct(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 5)
    public void placeOrderWithTimeout(Order order) {
        placeOrderAndUpdateInventory(order);

        try {
            logger.info("Entering fake sleep for 10s");
            Thread.sleep(10_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            logger.info("Exiting fake sleep for 10s");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void placeOrderWithoutTimeout(Order order) {
        placeOrderAndUpdateInventory(order);
    }

    private void placeOrderAndUpdateInventory(Order order) {
        Assert.isTrue(!TransactionSynchronizationManager.isCurrentTransactionReadOnly(), "Read-only");
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "No tx");

        // Update product inventories
        order.getOrderItems().forEach(orderItem -> {
            Product product = orderItem.getProduct();
            product.addInventoryQuantity(-orderItem.getQuantity());
            productRepository.save(product); // product is in detached state
        });
        order.setStatus(ShipmentStatus.confirmed);

        orderRepository.save(order);
    }
}
