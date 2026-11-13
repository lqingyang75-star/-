package com.example.stockpicker.finance.repository;

import com.example.stockpicker.finance.entity.FinRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FinRecordRepository extends JpaRepository<FinRecord, Long>, JpaSpecificationExecutor<FinRecord> {

    long countByUserIdAndCategoryId(Long userId, Long categoryId);

    @Query("select coalesce(sum(r.amount), 0) from FinRecord r "
            + "where r.userId = :userId and r.type = :type and r.recordDate between :start and :end")
    BigDecimal sumAmount(@Param("userId") Long userId,
                         @Param("type") Integer type,
                         @Param("start") LocalDate start,
                         @Param("end") LocalDate end);

    @Query("select r.categoryId, sum(r.amount) from FinRecord r "
            + "where r.userId = :userId and r.recordDate between :start and :end group by r.categoryId")
    List<Object[]> sumGroupByCategory(@Param("userId") Long userId,
                                      @Param("start") LocalDate start,
                                      @Param("end") LocalDate end);
}
