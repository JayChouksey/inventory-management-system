package com.example.coditas.tool.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tool_categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ToolCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 150)
    private String name;

    @Column(name = "description")
    private String description;
}
