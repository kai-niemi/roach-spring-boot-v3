package io.roach.spring.quartz.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.roach.spring.quartz.domain.PurchaseOrder;

@Repository
public interface OrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    @Modifying
    @Query(value = "delete from purchase_order_item where 1=1", nativeQuery = true)
    void deleteAllOrderItems();
}
