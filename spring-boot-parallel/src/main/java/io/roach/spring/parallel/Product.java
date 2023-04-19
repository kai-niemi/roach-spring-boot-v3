package io.roach.spring.parallel;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "product")
public class Product implements Persistable<UUID> {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Product instance = new Product();

        private Builder() {
        }

        public Builder withName(String name) {
            instance.name = name;
            return this;
        }

        public Builder withSku(String sku) {
            instance.sku = sku;
            return this;
        }

        public Builder withPrice(BigDecimal price) {
            instance.price = price;
            return this;
        }

        public Builder withQuantity(int quantity) {
            instance.inventory = quantity;
            return this;
        }


        public Product build() {
            return instance;
        }
    }

    @Transient
    private boolean isNew = true;


    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private UUID id;

    @Column(length = 128, nullable = false)
    private String name;

    @Column(length = 128, nullable = false, unique = true)
    private String sku;

    @Column(length = 25, nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private int inventory;

    @Column
    private String country;

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public UUID getId() {
        return id;
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

    public int addInventoryQuantity(int qty) {
        this.inventory += qty;
        return inventory;
    }

    public int getInventory() {
        return inventory;
    }
}
