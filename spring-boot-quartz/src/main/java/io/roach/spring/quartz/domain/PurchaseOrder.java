package io.roach.spring.quartz.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "purchase_order")
public class PurchaseOrder extends AbstractEntity<UUID> {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<PurchaseOrderItem> purchaseOrderItems = new ArrayList<>();

        private Customer customer;

        private Builder() {
        }

        public Builder withCustomer(Customer customer) {
            this.customer = customer;
            return this;
        }

        public PurchaseOrderItem.NestedBuilder andOrderItem() {
            return new PurchaseOrderItem.NestedBuilder(this, purchaseOrderItems::add);
        }

        public PurchaseOrder build() {
            if (this.purchaseOrderItems.isEmpty()) {
                throw new IllegalStateException("Empty order");
            }
            Assert.notNull(customer, "Customer must not be null");

            PurchaseOrder purchaseOrder = new PurchaseOrder();
            purchaseOrder.customer = customer;
            purchaseOrder.totalPrice = purchaseOrder.subTotal();
            purchaseOrder.purchaseOrderItems.addAll(this.purchaseOrderItems);
            return purchaseOrder;
        }
    }

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID",
            type = org.hibernate.id.UUIDGenerator.class)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "total_price", nullable = false, updatable = false)
    private BigDecimal totalPrice;

    @ElementCollection(fetch = FetchType.LAZY)
    @JoinTable(
            name = "purchase_order_item",
            joinColumns = @JoinColumn(name = "order_id")
    )
    @OrderColumn(name = "item_pos")
    private List<PurchaseOrderItem> purchaseOrderItems = new ArrayList<>();

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

    public PurchaseOrder setStatus(ShipmentStatus status) {
        this.status = status;
        this.dateUpdated = LocalDateTime.now();
        return this;
    }

    public List<PurchaseOrderItem> getOrderItems() {
        return Collections.unmodifiableList(purchaseOrderItems);
    }

    public BigDecimal subTotal() {
        BigDecimal subTotal = BigDecimal.ZERO;
        for (PurchaseOrderItem oi : purchaseOrderItems) {
            subTotal = subTotal.add(oi.totalCost());
        }
        return subTotal;
    }
}
