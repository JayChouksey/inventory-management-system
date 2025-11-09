package com.example.coditas.product.repository;

import com.example.coditas.product.entity.ProductStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductStockRepository extends JpaRepository<ProductStock, Long> {

    @Query("SELECT COALESCE(SUM(ps.quantity), 0) FROM ProductStock ps WHERE ps.product.id = :productId")
    Integer sumQuantityByProductId(@Param("productId") Long productId);
}
