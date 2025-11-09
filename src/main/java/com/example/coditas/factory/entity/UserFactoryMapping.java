package com.example.coditas.factory.entity;

import com.example.coditas.appuser.entity.Role;
import com.example.coditas.appuser.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_factory_mapping",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "factory_id", "assigned_role"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserFactoryMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    @ManyToOne @JoinColumn(name = "bay_id")
    private FactoryBay bay;

    @ManyToOne @JoinColumn(name = "assigned_role")
    private Role assignedRole;
}
