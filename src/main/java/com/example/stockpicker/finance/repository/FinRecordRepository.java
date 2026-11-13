package com.example.stockpicker.finance.repository;

import com.example.stockpicker.finance.entity.FinRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FinRecordRepository
        extends JpaRepository<FinRecord, Long>, JpaSpecificationExecutor<FinRecord> {

    long countByUserIdAndCategoryId(Long userId, Long categoryId);
}
