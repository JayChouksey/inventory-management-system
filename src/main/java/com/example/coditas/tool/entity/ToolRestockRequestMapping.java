package com.example.coditas.tool.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tool_restock_request_mapping")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ToolRestockRequestMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne @JoinColumn(name = "tool_id", nullable = false)
    private Tool tool;

    @Column(nullable = false)
    private Integer quantity = 0;

    @ManyToOne @JoinColumn(name = "request_id", nullable = false)
    private ToolRestockRequest request;
}
