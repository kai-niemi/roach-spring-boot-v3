package io.roach.spring.quartz.domain;

import java.math.BigDecimal;
import java.util.function.Consumer;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Embeddable
public class PurchaseOrderItem {
    public static final class NestedBuilder {
        private final PurchaseOrder.Builder parentBuilder;

        private final Consumer<PurchaseOrderItem> callback;

        private int quantity;

        private BigDecimal unitPrice;

        private Product product;

        NestedBuilder(PurchaseOrder.Builder parentBuilder, Consumer<PurchaseOrderItem> callback) {
            this.parentBuilder = parentBuilder;
            this.callback = callback;
        }

        public NestedBuilder withQuantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public NestedBuilder withUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }

        public NestedBuilder withProduct(Product product) {
            this.product = product;
            return this;
        }

        public PurchaseOrder.Builder then() {
            if (this.unitPrice == null) {
                this.unitPrice = product.getPrice();
            }

            PurchaseOrderItem purchaseOrderItem = new PurchaseOrderItem();
            purchaseOrderItem.product = this.product;
            purchaseOrderItem.unitPrice = this.unitPrice;
            purchaseOrderItem.quantity = this.quantity;

            callback.accept(purchaseOrderItem);

            return parentBuilder;
        }
    }

    @Column(nullable = false, updatable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, updatable = false)
    private BigDecimal unitPrice;

    @ManyToOne(fetch = FetchType.LAZY)  // Default fetch type is EAGER for @ManyToOne
    @JoinColumn(name = "product_id", updatable = false)
    @Fetch(FetchMode.JOIN)
    private Product product;

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public Product getProduct() {
        return product;
    }

    public BigDecimal totalCost() {
        if (unitPrice == null) {
            throw new IllegalStateException("unitPrice is null");
        }
        return unitPrice.multiply(new BigDecimal(quantity));
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", product=" + product +
                '}';
    }
}
