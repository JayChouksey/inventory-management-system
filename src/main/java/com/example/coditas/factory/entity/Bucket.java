package com.example.coditas.factory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bucket")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bucket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bucket_id", nullable = false, unique = true, length = 50)
    private String bucketId;

    @Column(name = "stack_no", length = 50)
    private String stackNo;

    @Column(length = 50)
    private String col;

    @Column(length = 50)
    private String row;

    @ManyToOne
    @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;
}
