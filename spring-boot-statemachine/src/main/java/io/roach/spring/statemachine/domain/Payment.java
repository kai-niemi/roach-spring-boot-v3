package io.roach.spring.statemachine.domain;

import java.math.BigDecimal;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;

@Entity
@Table(name = "payment")
@DynamicInsert
@DynamicUpdate
public class Payment extends AbstractEntity<Long> {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Payment instance = new Payment();

        private Builder() {
        }

        public Builder withId(Long id) {
            instance.id = id;
            return this;
        }

        public Builder withState(PaymentState state) {
            instance.state = state;
            return this;
        }

        public Builder withAmount(BigDecimal amount) {
            instance.amount = amount;
            return this;
        }

        public Builder withMerchant(String merchant) {
            instance.merchant = merchant;
            return this;
        }

        public Payment build() {
            return instance;
        }
    }

    @Id
    @Column(updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PaymentState state;

    @Column
    private String merchant;

    @Column
    private BigDecimal amount;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public PaymentState getState() {
        return state;
    }

    public void setState(PaymentState state) {
        this.state = state;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
