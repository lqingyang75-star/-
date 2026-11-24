package com.example.stockpicker.stock.repository;

import com.example.stockpicker.stock.entity.WatchStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WatchStockRepository extends JpaRepository<WatchStock, Long> {

    List<WatchStock> findByUserIdOrderByIdAsc(Long userId);

    Optional<WatchStock> findByUserIdAndStockCode(Long userId, String stockCode);

    boolean existsByUserIdAndStockCode(Long userId, String stockCode);

    long countByUserId(Long userId);
}
