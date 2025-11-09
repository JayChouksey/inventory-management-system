package com.example.coditas.centraloffice.repository;

import com.example.coditas.centraloffice.entity.UserCentralOfficeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCentralOfficeMappingRepository extends JpaRepository<UserCentralOfficeMapping, Long> {
}
