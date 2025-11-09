package com.example.coditas.product.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_category")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    private String description;
}
