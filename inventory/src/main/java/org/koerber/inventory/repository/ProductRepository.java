package org.koerber.inventory.repository;

import org.koerber.inventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Product entity
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find product by product code
     * @param productCode unique product code
     * @return Optional of Product
     */
    Optional<Product> findByProductCode(String productCode);
}
