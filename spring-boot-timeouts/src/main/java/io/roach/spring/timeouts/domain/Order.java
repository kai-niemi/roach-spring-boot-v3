package io.roach.spring.timeouts.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
//@TypeDef(name = "custom_enum", typeClass = ShipmentStatusEnumType.class)
public class Order extends AbstractEntity<UUID> {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<OrderItem> orderItems = new ArrayList<>();

        private Builder() {
        }

        public OrderItem.NestedBuilder andOrderItem() {
            return new OrderItem.NestedBuilder(this, orderItems::add);
        }

        public Order build() {
            if (this.orderItems.isEmpty()) {
                throw new IllegalStateException("Empty order");
            }

            Order order = new Order();
            order.totalPrice = order.subTotal();
            order.orderItems.addAll(this.orderItems);
            return order;
        }
    }

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(name = "total_price", nullable = false, updatable = false)
    private BigDecimal totalPrice;

    @ElementCollection(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JoinTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    @OrderColumn(name = "item_pos")
    private List<OrderItem> orderItems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 25, nullable = false)
    private ShipmentStatus status = ShipmentStatus.placed;

    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false, updatable = false, name = "date_placed")
    private LocalDateTime datePlaced;

    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false, name = "date_updated")
    private LocalDateTime dateUpdated;

    @PrePersist
    protected void onCreate() {
        if (datePlaced == null) {
            datePlaced = LocalDateTime.now();
        }
        if (dateUpdated == null) {
            dateUpdated = LocalDateTime.now();
        }
    }

    @Override
    public UUID getId() {
        return id;
    }

    public Order setStatus(ShipmentStatus status) {
        this.status = status;
        this.dateUpdated = LocalDateTime.now();
        return this;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public LocalDateTime getDatePlaced() {
        return datePlaced;
    }

    public LocalDateTime getDateUpdated() {
        return dateUpdated;
    }

    public Order setId(UUID id) {
        this.id = id;
        return this;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(orderItems);
    }

    public BigDecimal subTotal() {
        BigDecimal subTotal = BigDecimal.ZERO;
        for (OrderItem oi : orderItems) {
            subTotal = subTotal.add(oi.totalCost());
        }
        return subTotal;
    }
}
