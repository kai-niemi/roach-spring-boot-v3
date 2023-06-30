package io.roach.spring.timeouts.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

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
                .orElseThrow(() -> new ObjectRetrievalFailureException(Product.class, sku));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 5)
    public void placeOrderWithTimeout(Order order, long delayMillis) {
        placeOrderAndUpdateInventory(order);

        try {
            logger.info("Entering sleep for " + delayMillis);
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            logger.info("Exited sleep for " + delayMillis);
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
