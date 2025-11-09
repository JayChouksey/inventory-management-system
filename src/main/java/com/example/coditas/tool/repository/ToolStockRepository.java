package com.example.coditas.tool.repository;

import com.example.coditas.tool.entity.ToolStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ToolStockRepository extends JpaRepository<ToolStock, Long> {

    @Query("SELECT COALESCE(SUM(ts.totalQuantity), 0) FROM ToolStock ts WHERE ts.tool.id = :toolId")
    Integer sumTotalQuantityByToolId(@Param("toolId") Long toolId);

    @Query("SELECT COALESCE(SUM(ts.availableQuantity), 0) FROM ToolStock ts WHERE ts.tool.id = :toolId")
    Integer sumAvailableQuantityByToolId(@Param("toolId") Long toolId);
}
