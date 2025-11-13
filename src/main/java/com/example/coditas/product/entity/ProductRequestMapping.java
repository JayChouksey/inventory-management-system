package com.example.coditas.product.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_request_mapping")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductRequestMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "request_id", nullable = false)
    private ProductRequest request;

    @ManyToOne @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Long quantity = 0L;
}
