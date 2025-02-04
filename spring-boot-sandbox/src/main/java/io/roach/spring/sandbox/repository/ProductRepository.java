package io.roach.spring.sandbox.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import io.roach.spring.sandbox.domain.Product;

@Repository
public interface ProductRepository extends PagingAndSortingRepository<Product, UUID>,
        JpaRepository<Product,UUID> {
    @Query(value = "select id from product order by random() limit 1", nativeQuery = true)
    Optional<UUID> findByRandomId();

    @Query
    Optional<Product> findBySku(String sku);
}
