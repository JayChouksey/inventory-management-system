package com.example.coditas.tool.entity;

import com.example.coditas.factory.entity.Bucket;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tools_bucket_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolsBucketMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "tool_id", nullable = false)
    private Tool tool;

    @ManyToOne @JoinColumn(name = "bucket_id", nullable = false)
    private Bucket bucket;

    @Column(nullable = false)
    private Integer quantity = 0;
}
