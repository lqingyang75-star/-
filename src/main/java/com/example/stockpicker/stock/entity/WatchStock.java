package com.example.stockpicker.stock.entity;

import com.example.stockpicker.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * 自选股。按用户隔离，仅存代码与名称，行情与评分在查询时实时获取。
 */
@Entity
@Table(name = "watch_stock")
@SQLDelete(sql = "UPDATE watch_stock SET is_deleted = 1 WHERE id = ?")
@SQLRestriction("is_deleted = 0")
public class WatchStock extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "stock_code", nullable = false, length = 10)
    private String stockCode;

    @Column(name = "stock_name", length = 50)
    private String stockName;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }
}
