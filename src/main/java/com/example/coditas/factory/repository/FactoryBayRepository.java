package com.example.coditas.factory.repository;

import com.example.coditas.factory.entity.FactoryBay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FactoryBayRepository extends JpaRepository<FactoryBay, Long> {
}
