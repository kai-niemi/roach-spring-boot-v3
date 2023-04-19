package io.roach.spring.parallel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
@Transactional(propagation = Propagation.SUPPORTS)
public interface ProductRepository extends JpaRepository<Product, UUID> {
    @Query("select sum(p.inventory) from Product p where p.country = :country")
    Integer sumInventory(@Param("country") String country);

    @Query("select sum(p.inventory) from Product p")
    Integer sumTotalInventory();
}
