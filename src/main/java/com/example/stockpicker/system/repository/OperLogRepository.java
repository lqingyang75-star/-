package com.example.stockpicker.system.repository;

import com.example.stockpicker.system.entity.OperLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OperLogRepository extends JpaRepository<OperLog, Long>, JpaSpecificationExecutor<OperLog> {
}
