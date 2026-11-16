package com.example.stockpicker.system.repository;

import com.example.stockpicker.system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    /** 角色被多少用户引用（软删除用户已被过滤） */
    long countByRolesId(Long roleId);

    /** 软删除不会自动清理关联表，需手动删除 */
    @Modifying
    @Query(value = "delete from sys_user_role where user_id = :userId", nativeQuery = true)
    void removeRoleRefs(@Param("userId") Long userId);

    List<User> findAllByOrderByIdAsc();
}
