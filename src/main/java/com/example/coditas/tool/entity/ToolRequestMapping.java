package com.example.coditas.tool.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tool_request_mapping")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ToolRequestMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "request_id", nullable = false)
    private ToolRequest request;

    @ManyToOne
    @JoinColumn(name = "tool_id", nullable = false)
    private Tool tool;

    @Column(name = "quantity_requested", nullable = false)
    private Integer quantityRequested = 0;
}
