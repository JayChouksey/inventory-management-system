package com.example.coditas.product.repository;

import com.example.coditas.factory.entity.Factory;
import com.example.coditas.product.entity.Product;
import com.example.coditas.product.entity.ProductStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductStockRepository extends JpaRepository<ProductStock, Long> {

    @Query("SELECT COALESCE(SUM(ps.quantity), 0) FROM ProductStock ps WHERE ps.product.id = :productId")
    Integer sumQuantityByProductId(@Param("productId") Long productId);

    Optional<ProductStock> findByFactoryAndProduct(Factory factory, Product product);

    Page<ProductStock> findByFactory(Factory factory, Pageable pageable);

    Page<ProductStock> findByProduct(Product product, Pageable pageable);
}
