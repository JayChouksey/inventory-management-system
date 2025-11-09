package com.example.coditas.product.repository;

import com.example.coditas.product.entity.FactoryProduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

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

    List<FactoryProduction> findByFactoryIdAndProductionDateBetween(
            Long factoryId,
            LocalDate start,
            LocalDate end
    );
}
