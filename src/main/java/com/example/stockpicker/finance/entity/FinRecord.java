package com.example.stockpicker.finance.entity;

import com.example.stockpicker.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 收支记录。通用收支字段满足「类型 + 分类 + 金额 + 日期 + 备注」，
 * 另挂一组可空的证券交易字段，用于记录个人股票买卖与手续费。
 *
 * 金额规则：
 *   买入（type=2 支出）：amount = 成交额 + 佣金 + 印花税 + 过户费
 *   卖出（type=1 收入）：amount = 成交额 - 佣金 - 印花税 - 过户费
 */
@Entity
@Table(name = "fin_record")
@SQLDelete(sql = "UPDATE fin_record SET is_deleted = 1 WHERE id = ?")
@SQLRestriction("is_deleted = 0")
public class FinRecord extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    /** 1 收入 2 支出 */
    @Column(nullable = false)
    private Integer type;

    /** 记账金额，DECIMAL(14,2)，禁止用 double */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(length = 255)
    private String remark;

    // ============ 证券交易扩展字段（非交易记录时为空） ============

    /** 股票代码 */
    @Column(name = "stock_code", length = 10)
    private String stockCode;

    /** BUY 买入 / SELL 卖出 */
    @Column(name = "trade_side", length = 4)
    private String tradeSide;

    /** 成交数量（股） */
    @Column
    private Integer quantity;

    /** 成交价，支持 3 位小数 */
    @Column(name = "trade_price", precision = 14, scale = 3)
    private BigDecimal tradePrice;

    /** 佣金 */
    @Column(precision = 14, scale = 2)
    private BigDecimal commission;

    /** 印花税（仅卖出收取） */
    @Column(name = "stamp_duty", precision = 14, scale = 2)
    private BigDecimal stampDuty;

    /** 过户费 */
    @Column(name = "transfer_fee", precision = 14, scale = 2)
    private BigDecimal transferFee;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public String getTradeSide() {
        return tradeSide;
    }

    public void setTradeSide(String tradeSide) {
        this.tradeSide = tradeSide;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTradePrice() {
        return tradePrice;
    }

    public void setTradePrice(BigDecimal tradePrice) {
        this.tradePrice = tradePrice;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }

    public BigDecimal getStampDuty() {
        return stampDuty;
    }

    public void setStampDuty(BigDecimal stampDuty) {
        this.stampDuty = stampDuty;
    }

    public BigDecimal getTransferFee() {
        return transferFee;
    }

    public void setTransferFee(BigDecimal transferFee) {
        this.transferFee = transferFee;
    }
}
