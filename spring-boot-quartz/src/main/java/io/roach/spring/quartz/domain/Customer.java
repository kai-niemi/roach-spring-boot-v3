package io.roach.spring.quartz.domain;

import java.util.UUID;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer")
@NamedQueries({
        @NamedQuery(
                name = Customer.QUERY_BY_USERNAME,
                query = "from Customer u "
                        + "where u.userName = :userName"
        )
})
@DynamicInsert
@DynamicUpdate
public class Customer extends AbstractEntity<UUID> {
    public static final String QUERY_BY_USERNAME = "Customer.findByName";

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            type = org.hibernate.id.UUIDGenerator.class
    )
    private UUID id;

    @Column(length = 15, nullable = false, unique = true, name = "user_name")
    private String userName;

    @Column(length = 128, name = "first_name")
    private String firstName;

    @Column(length = 128, name = "last_name")
    private String lastName;

    @Column(length = 128)
    private String email;

    public Customer() {
    }

    public Customer(String userName) {
        this.userName = userName;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
