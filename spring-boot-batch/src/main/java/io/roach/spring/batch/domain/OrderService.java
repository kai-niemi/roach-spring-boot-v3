package io.roach.spring.batch.domain;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

@Service
public class OrderService {
    private static <T> Stream<List<T>> chunkedStream(Stream<T> stream, int chunkSize) {
        AtomicInteger idx = new AtomicInteger();
        return stream.collect(Collectors.groupingBy(x -> idx.getAndIncrement() / chunkSize))
                .values().stream();
    }

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void placeOrder(Order order) {
        Assert.isTrue(!TransactionSynchronizationManager.isCurrentTransactionReadOnly(), "Read-only");
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "No tx");
        orderRepository.save(order);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void placeOrderAndUpdateInventory(Collection<Order> orders) {
        Stream<List<Order>> chunked = chunkedStream(orders.stream(), 150);
        chunked.forEach(chunk -> {
            chunk.forEach(this::placeOrderAndUpdateInventory);
            orderRepository.flush();
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void placeOrderAndUpdateInventory(Order order) {
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void placeOrders(Collection<Order> orders) {
        Assert.isTrue(!TransactionSynchronizationManager.isCurrentTransactionReadOnly(), "Read-only");
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "No tx");
        orderRepository.saveAll(orders);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateOrderStatus(Iterable<UUID> ids, ShipmentStatus status) {
        Assert.isTrue(!TransactionSynchronizationManager.isCurrentTransactionReadOnly(), "Read-only");
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "No tx");

        orderRepository.findAllById(ids).forEach(order -> order.setStatus(status));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public List<UUID> findOrderIdsByStatus(ShipmentStatus status) {
        return orderRepository.findIdsByShipmentStatus(status);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public List<Order> findOrdersByStatus(ShipmentStatus status, int limit) {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "No tx");

        List<Order> orders = orderRepository.findByShipmentStatus(status,
                Pageable.ofSize(limit)).getContent();
        // Lazy-fetch associations
        orders.forEach(order -> order.getOrderItems().size());
        return orders;
    }
}
