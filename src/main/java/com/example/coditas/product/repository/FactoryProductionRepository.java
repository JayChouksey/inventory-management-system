package com.example.coditas.product.repository;

import com.example.coditas.factory.entity.Factory;
import com.example.coditas.product.entity.FactoryProduction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FactoryProductionRepository extends JpaRepository<FactoryProduction, Long> {

    @Query("SELECT COALESCE(SUM(fp.productionQuantity), 0) " +
            "FROM FactoryProduction fp " +
            "WHERE fp.factory.id = :factoryId " +
            "AND fp.productionDate BETWEEN :start AND :end")
    Long sumQuantityByFactoryAndDateRange(
            @Param("factoryId") Long factoryId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    Page<FactoryProduction> findByFactory(Factory factory, Pageable pageable);
}
