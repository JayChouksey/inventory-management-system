package com.example.coditas.factory.entity;

import com.example.coditas.user.entity.User;
import com.example.coditas.centraloffice.entity.CentralOffice;
import com.example.coditas.common.enums.ActiveStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "factory")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Factory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "factory_id", nullable = false, unique = true, length = 50)
    private String factoryId;

    @Column(nullable = false, length = 255)
    private String name;

    private String city;
    private String address;

    @ManyToOne
    @JoinColumn(name = "plant_head_id")
    private User plantHead;

    @ManyToOne
    @JoinColumn(name = "central_office_id")
    private CentralOffice centralOffice;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_active", nullable = false)
    private ActiveStatus isActive = ActiveStatus.ACTIVE;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @OneToMany(mappedBy = "factory", cascade = CascadeType.ALL)
    private List<FactoryBay> bays;

    @OneToMany(mappedBy = "factory")
    private List<Bucket> buckets;

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