package com.example.coditas.order.repository;

import com.example.coditas.order.entity.DealerOrder;
import com.example.coditas.order.enums.DealerOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;

@Repository
public interface DealerOrderRepository extends JpaRepository<DealerOrder, Long> {

    @Query("""
        SELECT COALESCE(SUM(o.totalPrice), 0.0)
        FROM DealerOrder o
        WHERE o.createdAt BETWEEN :start AND :end
          AND o.status = :status
        """)
    Optional<BigDecimal> sumTotalPriceByDateRangeAndStatus(
            @Param("start") ZonedDateTime start,
            @Param("end") ZonedDateTime end,
            @Param("status") DealerOrderStatus status
    );

    @Query("SELECT COUNT(o) FROM DealerOrder o WHERE o.centralOffice.id = :officeId")
    Long countOrdersByCentralOfficeId(@Param("officeId") Long officeId);

    @Modifying
    @Query("UPDATE DealerOrder o SET o.centralOffice.id = :newOfficeId WHERE o.centralOffice.id = :oldOfficeId")
    void reassignOrders(@Param("oldOfficeId") Long oldOfficeId, @Param("newOfficeId") Long newOfficeId);
}