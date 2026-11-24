package com.example.stockpicker.system.repository;

import com.example.stockpicker.system.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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

    /** 把新菜单授予超级管理员角色，已授权时跳过（兼容老库升级） */
    @Transactional
    @Modifying
    @Query(value = "insert into sys_role_menu (role_id, menu_id) "
            + "select r.id, :menuId from sys_role r "
            + "where r.code = 'ADMIN' and r.is_deleted = 0 "
            + "and not exists (select 1 from sys_role_menu rm where rm.role_id = r.id and rm.menu_id = :menuId)",
            nativeQuery = true)
    void grantMenuToAdmin(@Param("menuId") Long menuId);
}
