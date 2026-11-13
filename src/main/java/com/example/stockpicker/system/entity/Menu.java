package com.example.stockpicker.system.entity;

import com.example.stockpicker.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * 菜单与按钮。type：1 目录 2 菜单 3 按钮。
 * 菜单驱动前端动态路由与侧边栏，按钮提供按钮级权限码。
 */
@Entity
@Table(name = "sys_menu")
@SQLDelete(sql = "UPDATE sys_menu SET is_deleted = 1 WHERE id = ?")
@SQLRestriction("is_deleted = 0")
public class Menu extends BaseEntity {

    /** 父级 id，0 表示一级 */
    @Column(name = "parent_id", nullable = false)
    private Long parentId = 0L;

    @Column(nullable = false, length = 50)
    private String name;

    /** 1 目录 2 菜单 3 按钮 */
    @Column(nullable = false)
    private Integer type;

    @Column(length = 100)
    private String path;

    /** 前端组件路径，如 record/list */
    @Column(length = 120)
    private String component;

    /** 权限标识，如 fin:record:add */
    @Column(length = 100)
    private String perms;

    @Column(length = 50)
    private String icon;

    @Column(nullable = false)
    private Integer sort = 0;

    /** 1 启用 0 停用 */
    @Column(nullable = false)
    private Integer status = 1;

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getPerms() {
        return perms;
    }

    public void setPerms(String perms) {
        this.perms = perms;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
