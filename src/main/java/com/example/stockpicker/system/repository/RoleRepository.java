package com.example.stockpicker.system.repository;

import com.example.stockpicker.system.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @Modifying
    @Query(value = "delete from sys_user_role where role_id = :roleId", nativeQuery = true)
    void removeUserRefs(@Param("roleId") Long roleId);

    @Modifying
    @Query(value = "delete from sys_role_menu where role_id = :roleId", nativeQuery = true)
    void removeMenuRefs(@Param("roleId") Long roleId);

    @Modifying
    @Query(value = "delete from sys_role_menu where menu_id = :menuId", nativeQuery = true)
    void removeMenuRefsByMenuId(@Param("menuId") Long menuId);
}
