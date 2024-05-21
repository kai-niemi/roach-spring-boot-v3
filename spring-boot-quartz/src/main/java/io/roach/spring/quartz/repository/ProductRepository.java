package io.roach.spring.quartz.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.roach.spring.quartz.domain.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    @Query
    Optional<Product> findBySku(String sku);
}
