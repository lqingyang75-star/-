package com.example.stockpicker.finance.repository;

import com.example.stockpicker.finance.entity.FinCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinCategoryRepository extends JpaRepository<FinCategory, Long> {

    List<FinCategory> findByUserIdAndTypeOrderBySortAscIdAsc(Long userId, Integer type);

    List<FinCategory> findByUserIdOrderByTypeAscSortAscIdAsc(Long userId);

    boolean existsByUserIdAndNameAndType(Long userId, String name, Integer type);
}
