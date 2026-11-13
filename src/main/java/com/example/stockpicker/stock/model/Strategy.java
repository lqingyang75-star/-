package com.example.stockpicker.stock.model;

/**
 * 选股策略：不同投资风格对估值/盈利/动量因子赋予不同权重。
 *
 * <p>四个因子的归一化取值均在 0~1：
 * <ul>
 *     <li>PE 因子：PE 越低分越高（30 倍记 0 分，趋近 0 记满分），亏损（PE&le;0）记 0 分</li>
 *     <li>PB 因子：PB 越低分越高（5 倍记 0 分，趋近 0 记满分）</li>
 *     <li>ROE 因子：ROE 越高分越高（25% 记满分）</li>
 *     <li>动量因子：当日涨跌幅 -3%~+3% 映射到 0~1</li>
 * </ul>
 */
public enum Strategy {

    /** 价值型：重估值，偏好低 PE、低 PB。 */
    VALUE("价值型", 0.45, 0.35, 0.20, 0.00),

    /** 成长型：重盈利与动量，偏好高 ROE、上涨趋势。 */
    GROWTH("成长型", 0.20, 0.00, 0.50, 0.30),

    /** 均衡型：估值、盈利、动量兼顾。 */
    BALANCED("均衡型", 0.30, 0.20, 0.30, 0.20);

    private final String label;
    private final double peWeight;
    private final double pbWeight;
    private final double roeWeight;
    private final double momentumWeight;

    Strategy(String label, double peWeight, double pbWeight,
             double roeWeight, double momentumWeight) {
        this.label = label;
        this.peWeight = peWeight;
        this.pbWeight = pbWeight;
        this.roeWeight = roeWeight;
        this.momentumWeight = momentumWeight;
    }

    public String label() {
        return label;
    }

    public double peWeight() {
        return peWeight;
    }

    public double pbWeight() {
        return pbWeight;
    }

    public double roeWeight() {
        return roeWeight;
    }

    public double momentumWeight() {
        return momentumWeight;
    }

    /** 入参为策略代码（忽略大小写），无法识别时回退到均衡型。 */
    public static Strategy from(String code) {
        if (code == null || code.isBlank()) {
            return BALANCED;
        }
        for (Strategy strategy : values()) {
            if (strategy.name().equalsIgnoreCase(code.trim())) {
                return strategy;
            }
        }
        return BALANCED;
    }
}
