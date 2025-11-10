package com.example.coditas.centraloffice.repository;

import com.example.coditas.appuser.entity.User;
import com.example.coditas.centraloffice.entity.CentralOffice;
import com.example.coditas.common.enums.ActiveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CentralOfficeRepository extends JpaRepository<CentralOffice, Long> {
    Optional<CentralOffice> findByCentralOfficeId(String userId);
    long countByIsActive(ActiveStatus status);
    Optional<CentralOffice> findFirstByIsActiveAndCentralOfficeIdNot(ActiveStatus status, String excludeId);

    Page<CentralOffice> findAll(Specification<CentralOffice> spec, Pageable pageable);
}