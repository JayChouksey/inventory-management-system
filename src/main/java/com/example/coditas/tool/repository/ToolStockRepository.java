package com.example.coditas.tool.repository;

import com.example.coditas.factory.entity.Factory;
import com.example.coditas.tool.entity.Tool;
import com.example.coditas.tool.entity.ToolStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ToolStockRepository extends JpaRepository<ToolStock, Long> {

    @Query("SELECT COALESCE(SUM(ts.totalQuantity), 0) FROM ToolStock ts WHERE ts.tool.id = :toolId")
    Integer sumTotalQuantityByToolId(@Param("toolId") Long toolId);

    @Query("SELECT COALESCE(SUM(ts.availableQuantity), 0) FROM ToolStock ts WHERE ts.tool.id = :toolId")
    Integer sumAvailableQuantityByToolId(@Param("toolId") Long toolId);


    Optional<ToolStock> findByFactoryAndTool(Factory factory, Tool tool);
    Page<ToolStock> findByFactory(Factory factory, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ToolStock ts SET ts.totalQuantity = ts.totalQuantity + :quantity, ts.availableQuantity = ts.availableQuantity + :quantity WHERE ts.id = :stockId")
    void incrementStock(@Param("stockId") Long stockId, @Param("quantity") Long quantity);

    @Modifying(clearAutomatically = true)
    @Query(value = "INSERT INTO tool_stock (factory_id, tool_id, total_quantity, available_quantity, issued_quantity, last_updated_at) VALUES (:factoryId, :toolId, :quantity, :quantity, 0, NOW())", nativeQuery = true)
    void insertNewStock(@Param("factoryId") Long factoryId, @Param("toolId") Long toolId, @Param("quantity") Long quantity);
}
