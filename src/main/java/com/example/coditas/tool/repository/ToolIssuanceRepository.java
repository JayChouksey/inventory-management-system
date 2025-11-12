package com.example.coditas.tool.repository;

import com.example.coditas.factory.entity.Factory;
import com.example.coditas.tool.entity.ToolIssuance;
import com.example.coditas.tool.enums.ToolIssuanceStatus;
import com.example.coditas.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;// ToolIssuanceRepository.java

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ToolIssuanceRepository extends JpaRepository<ToolIssuance, Long> {
    Page<ToolIssuance> findByWorkerAndStatusIn(User worker, List<ToolIssuanceStatus> statuses, Pageable pageable);

    @Query("SELECT ti FROM ToolIssuance ti WHERE ti.factory = :factory " +
            "AND ti.returnDate < :overdueDate " +
            "AND ti.status IN :statuses")
    Page<ToolIssuance> findOverdueTools(
            @Param("factory") Factory factory,
            @Param("overdueDate") LocalDateTime overdueDate,
            @Param("statuses") List<ToolIssuanceStatus> statuses,
            Pageable pageable
    );

}
