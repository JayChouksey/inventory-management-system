package com.example.coditas.tool.entity;

import com.example.coditas.tool.enums.Expensive;
import com.example.coditas.tool.enums.Perishable;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;

@Entity
@Table(name = "tools")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Tool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tool_id", nullable = false, unique = true, length = 50)
    private String toolId;

    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private ToolCategory category;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_perishable", nullable = false)
    private Perishable isPerishable = Perishable.NON_PERISHABLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_expensive", nullable = false)
    private Expensive isExpensive = Expensive.INEXPENSIVE;

    @Column(name = "threshold", nullable = false)
    private Integer threshold = 0;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @PrePersist
    protected void onCreate(){
        createdAt = ZonedDateTime.now();
        updatedAt = ZonedDateTime.now();
    }

    @PreUpdate
    protected void onUpdate(){
        updatedAt = ZonedDateTime.now();
    }
}