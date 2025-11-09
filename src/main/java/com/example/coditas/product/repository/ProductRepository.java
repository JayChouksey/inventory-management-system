package com.example.coditas.product.repository;

import com.example.coditas.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    Optional<Product> findActiveByProductId(String id);

    Optional<Product> findByProductId(String id);
}
