package com.example.stockpicker.finance.repository;

import com.example.stockpicker.finance.entity.FinCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinCategoryRepository extends JpaRepository<FinCategory, Long> {

    List<FinCategory> findByUserIdOrderBySortAscIdAsc(Long userId);

    boolean existsByUserIdAndNameAndType(Long userId, String name, Integer type);

    boolean existsByUserIdAndNameAndTypeAndIdNot(Long userId, String name, Integer type, Long id);
}
