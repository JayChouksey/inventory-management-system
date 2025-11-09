package com.example.coditas.product.entity;

import com.example.coditas.factory.entity.Factory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "factory_production")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FactoryProduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    @ManyToOne @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "production_quantity", nullable = false)
    private Integer productionQuantity = 0;

    @Column(name = "production_date", nullable = false)
    private LocalDate productionDate;
}
