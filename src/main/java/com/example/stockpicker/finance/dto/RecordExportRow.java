package com.example.stockpicker.finance.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;

import java.math.BigDecimal;

/**
 * Excel 导出行。列顺序与表头由 {@link ExcelProperty} 决定，中文表头避免乱码。
 */
@ColumnWidth(16)
public class RecordExportRow {

    @ExcelProperty("日期")
    private String recordDate;

    @ExcelProperty("类型")
    private String typeText;

    @ExcelProperty("分类")
    private String categoryName;

    @ExcelProperty("金额")
    private BigDecimal amount;

    @ExcelProperty("股票代码")
    private String stockCode;

    @ExcelProperty("买卖方向")
    private String tradeSideText;

    @ExcelProperty("数量")
    private Integer quantity;

    @ExcelProperty("成交价")
    private BigDecimal tradePrice;

    @ExcelProperty("佣金")
    private BigDecimal commission;

    @ExcelProperty("印花税")
    private BigDecimal stampDuty;

    @ExcelProperty("过户费")
    private BigDecimal transferFee;

    @ExcelProperty("备注")
    private String remark;

    public String getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(String recordDate) {
        this.recordDate = recordDate;
    }

    public String getTypeText() {
        return typeText;
    }

    public void setTypeText(String typeText) {
        this.typeText = typeText;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public String getTradeSideText() {
        return tradeSideText;
    }

    public void setTradeSideText(String tradeSideText) {
        this.tradeSideText = tradeSideText;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
