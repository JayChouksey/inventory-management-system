package com.example.coditas.centraloffice.entity;

import com.example.coditas.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_central_office_mapping")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserCentralOfficeMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne @JoinColumn(name = "central_office_id", nullable = false)
    private CentralOffice centralOffice;
}
