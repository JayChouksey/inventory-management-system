package com.example.coditas.centraloffice.repository;

import com.example.coditas.appuser.entity.User;
import com.example.coditas.centraloffice.entity.CentralOffice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CentralOfficeRepository extends JpaRepository<CentralOffice, Long> {
    Optional<CentralOffice> findByCentralOfficeId(String userId);
}