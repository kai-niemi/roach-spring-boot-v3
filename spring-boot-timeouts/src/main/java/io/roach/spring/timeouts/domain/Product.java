package io.roach.spring.timeouts.domain;

import java.math.BigDecimal;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product extends AbstractEntity<UUID> {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String name;

        private String sku;

        private BigDecimal price;

        private int quantity;

        private Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withSku(String sku) {
            this.sku = sku;
            return this;
        }

        public Builder withPrice(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Builder withQuantity(int quantity) {
            this.quantity = quantity;
            return this;
        }


        public Product build() {
            Product product = new Product();
            product.name = this.name;
            product.sku = this.sku;
            product.price = this.price;
            product.inventory = this.quantity;
            return product;
        }
    }

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private UUID id;

    @Column(length = 128, nullable = false)
    private String name;

    @NaturalId
    @Column(length = 128, nullable = false, unique = true)
    private String sku;

    @Column(length = 25, nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private int inventory;

    @Override
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getSku() {
        return sku;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal addPrice(BigDecimal price) {
        this.price = price.add(price);
        return this.price;
    }

    public int addInventoryQuantity(int qty) {
        this.inventory += qty;
        return this.inventory;
    }

    public int getInventory() {
        return inventory;
    }

    public void setInventory(int inventory) {
        this.inventory = inventory;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sku='" + sku + '\'' +
                ", price=" + price +
                ", inventory=" + inventory +
                '}';
    }
}
