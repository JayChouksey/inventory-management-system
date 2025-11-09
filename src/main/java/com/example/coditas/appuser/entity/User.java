package com.example.coditas.appuser.entity;

import com.example.coditas.common.enums.ActiveStatus;
import com.example.coditas.factory.entity.UserFactoryMapping;
import com.example.coditas.tool.entity.ToolRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true, length = 50)
    private String userId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 30)
    private String phone;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_active", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private ActiveStatus isActive =  ActiveStatus.ACTIVE;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "worker")
    private List<ToolRequest> toolRequests;

    @OneToMany(mappedBy = "user")
    private List<UserFactoryMapping> factoryMappings;

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
