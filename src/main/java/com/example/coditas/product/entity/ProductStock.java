package com.example.coditas.product.entity;

import com.example.coditas.factory.entity.Factory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_stock",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "factory_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    @Column(nullable = false)
    private Long quantity = 0L;
}
