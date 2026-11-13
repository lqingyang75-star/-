package com.example.stockpicker.system.repository;

import com.example.stockpicker.system.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findAllByOrderBySortAscIdAsc();
}
