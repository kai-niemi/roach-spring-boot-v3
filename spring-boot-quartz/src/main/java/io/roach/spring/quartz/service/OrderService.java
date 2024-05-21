package io.roach.spring.quartz.service;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import io.roach.spring.quartz.domain.Product;
import io.roach.spring.quartz.domain.PurchaseOrder;
import io.roach.spring.quartz.domain.ShipmentStatus;
import io.roach.spring.quartz.repository.OrderRepository;
import io.roach.spring.quartz.repository.ProductRepository;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Product findProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new ObjectRetrievalFailureException(Product.class, sku));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Retryable(exceptionExpression = "@exceptionClassifier.shouldRetry(#root)",
            maxAttempts = 5,
            backoff = @Backoff(maxDelay = 10_000, multiplier = 1.5),
            label = "placeOrder")
    public void placeOrder(PurchaseOrder purchaseOrder) {
        Assert.isTrue(!TransactionSynchronizationManager.isCurrentTransactionReadOnly(), "Read-only");
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "No tx");

        // Update product inventories
        purchaseOrder.getOrderItems().forEach(orderItem -> {
            Product product = orderItem.getProduct();
            product.addInventoryQuantity(-orderItem.getQuantity());

            productRepository.save(product);
        });
        purchaseOrder.setStatus(ShipmentStatus.confirmed);

        orderRepository.save(purchaseOrder);

        // Place a processing delay to cause high contention on the product inventory updates
        try {
            TimeUnit.MILLISECONDS.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
