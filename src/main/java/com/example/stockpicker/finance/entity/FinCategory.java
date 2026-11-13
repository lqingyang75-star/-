package com.example.stockpicker.finance.entity;

import com.example.stockpicker.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * 收支分类。按用户隔离，type：1 收入 2 支出。
 */
@Entity
@Table(name = "fin_category")
@SQLDelete(sql = "UPDATE fin_category SET is_deleted = 1 WHERE id = ?")
@SQLRestriction("is_deleted = 0")
public class FinCategory extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String name;

    /** 1 收入 2 支出 */
    @Column(nullable = false)
    private Integer type;

    @Column(nullable = false)
    private Integer sort = 0;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}
