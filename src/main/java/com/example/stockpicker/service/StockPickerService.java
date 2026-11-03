package com.example.stockpicker.service;

import com.example.stockpicker.model.CompareResult;
import com.example.stockpicker.model.PickResult;
import com.example.stockpicker.model.Stock;
import com.example.stockpicker.model.StockAnalysis;
import com.example.stockpicker.model.Strategy;
import com.example.stockpicker.model.StrategyScore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 选股服务：按策略规则给股票打分、排序，并生成筛选榜单与个股分析报告。
 *
 * <p>打分统一采用 0~100 的归一化模型：估值（PE/PB）、盈利（ROE）、动量（涨跌幅）
 * 四个因子按策略权重加权求和，权重见 {@link Strategy}。
 */
@Service
public class StockPickerService {

    private final RealTimeStockDataProvider realTimeProvider;

    public StockPickerService(RealTimeStockDataProvider realTimeProvider) {
        this.realTimeProvider = realTimeProvider;
    }

    /**
     * 按均衡型策略评分并排序返回。
     *
     * @param limit 返回条数上限
     */
    public List<PickResult> pick(int limit) {
        return pick(limit, null);
    }

    /**
     * 按均衡型策略评分并排序返回。
     *
     * @param limit 返回条数上限
     * @param codes 逗号分隔的股票代码；提供时用实时行情打分，缺省用内置样例
     */
    public List<PickResult> pick(int limit, String codes) {
        List<Stock> stocks;
        if (codes != null && !codes.isBlank()) {
            stocks = realTimeProvider.fetchByCodes(splitCodes(codes));
        } else {
            stocks = realTimeProvider.allStocks();
        }
        return rank(stocks, Strategy.BALANCED, limit);
    }

    /**
     * 智能选股：扫描内置股票池，按指定策略评分并返回排名靠前的股票。
     *
     * @param strategyCode 策略代码（value/growth/balanced），缺省均衡型
     * @param limit        返回条数上限
     */
    public List<PickResult> screen(String strategyCode, int limit) {
        Strategy strategy = Strategy.from(strategyCode);
        List<Stock> stocks = realTimeProvider.fetchByCodes(StockPool.codes());
        return rank(stocks, strategy, limit);
    }

    /**
     * 多策略对比：对同一批股票分别按 价值/成长/均衡 策略打分。
     *
     * @param codes 逗号分隔的股票代码
     */
    public List<CompareResult> compare(String codes) {
        List<Stock> stocks = realTimeProvider.fetchByCodes(splitCodes(codes));
        List<CompareResult> results = new ArrayList<>();
        for (Stock stock : stocks) {
            List<StrategyScore> scores = new ArrayList<>();
            for (Strategy strategy : Strategy.values()) {
                PickResult pick = score(stock, strategy);
                scores.add(new StrategyScore(
                        strategy.name().toLowerCase(),
                        strategy.label(),
                        pick.score(),
                        pick.reason()));
            }
            results.add(new CompareResult(stock, scores));
        }
        return results;
    }

    /**
     * 个股分析：生成一只股票的分析报告。
     *
     * @param code 6 位股票代码
     * @throws IllegalArgumentException 未取到行情时抛出
     */
    public StockAnalysis analyze(String code) {
        List<Stock> stocks = realTimeProvider.fetchByCodes(List.of(code));
        if (stocks.isEmpty()) {
            throw new IllegalArgumentException("未获取到该股票的行情数据，请检查代码是否正确。");
        }
        Stock stock = stocks.get(0);
        double scoreValue = score(stock, Strategy.BALANCED).score();
        String rating = rating(scoreValue);
        return new StockAnalysis(
                stock,
                scoreValue,
                rating,
                valuationText(stock),
                profitabilityText(stock),
                risks(stock),
                suggestion(rating));
    }

    // ---------- 打分 ----------

    private List<PickResult> rank(List<Stock> stocks, Strategy strategy, int limit) {
        List<PickResult> results = new ArrayList<>();
        for (Stock stock : stocks) {
            results.add(score(stock, strategy));
        }
        results.sort(Comparator.comparingDouble(PickResult::score).reversed());
        if (limit > 0 && results.size() > limit) {
            return results.subList(0, limit);
        }
        return results;
    }

    private PickResult score(Stock stock, Strategy strategy) {
        double vPe = stock.pe() > 0 ? clamp((30 - stock.pe()) / 30.0) : 0;
        double vPb = stock.pb() > 0 ? clamp((5 - stock.pb()) / 5.0) : 0;
        double vRoe = clamp(stock.roe() / 25.0);
        double vMomentum = clamp((stock.changePercent() + 3) / 6.0);

        double total = vPe * strategy.peWeight()
                + vPb * strategy.pbWeight()
                + vRoe * strategy.roeWeight()
                + vMomentum * strategy.momentumWeight();
        double value = Math.round(total * 10000) / 100.0;

        List<String> reasons = new ArrayList<>();
        if (stock.pe() > 0 && stock.pe() <= 30) {
            reasons.add("PE 较低(" + stock.pe() + ")");
        }
        if (stock.pb() > 0 && stock.pb() <= 5) {
            reasons.add("PB 较低(" + stock.pb() + ")");
        }
        if (stock.roe() >= 10) {
            reasons.add("ROE 较高(" + stock.roe() + "%)");
        }
        if (stock.changePercent() > 0) {
            reasons.add("今日上涨(" + stock.changePercent() + "%)");
        }
        String reason = reasons.isEmpty() ? "无明显亮点" : String.join("、", reasons);
        return new PickResult(stock, value, reason);
    }

    private String rating(double score) {
        if (score >= 75) {
            return "强烈推荐";
        }
        if (score >= 60) {
            return "推荐";
        }
        if (score >= 45) {
            return "中性";
        }
        if (score >= 30) {
            return "谨慎";
        }
        return "回避";
    }

    private String valuationText(Stock stock) {
        if (stock.pe() <= 0) {
            return "亏损，PE 为负，暂无有效估值参考";
        }
        if (stock.pe() < 15) {
            return "低估（PE " + stock.pe() + "，PB " + stock.pb() + "）";
        }
        if (stock.pe() <= 30) {
            return "合理（PE " + stock.pe() + "，PB " + stock.pb() + "）";
        }
        return "偏高（PE " + stock.pe() + "，PB " + stock.pb() + "）";
    }

    private String profitabilityText(Stock stock) {
        if (stock.roe() <= 0) {
            return "盈利较弱（ROE " + stock.roe() + "%）";
        }
        if (stock.roe() >= 20) {
            return "优秀（ROE " + stock.roe() + "%）";
        }
        if (stock.roe() >= 10) {
            return "良好（ROE " + stock.roe() + "%）";
        }
        return "一般（ROE " + stock.roe() + "%）";
    }

    private List<String> risks(Stock stock) {
        List<String> list = new ArrayList<>();
        if (stock.pe() <= 0) {
            list.add("公司当前处于亏损状态，估值指标失效");
        } else if (stock.pe() > 50) {
            list.add("市盈率偏高（PE " + stock.pe() + "），估值存在透支风险");
        }
        if (stock.pb() > 8) {
            list.add("市净率偏高（PB " + stock.pb() + "）");
        }
        if (stock.roe() <= 0) {
            list.add("净资产收益率为负，盈利能力承压");
        }
        if (stock.changePercent() <= -5) {
            list.add("当日跌幅较大（" + stock.changePercent() + "%），短期波动风险高");
        }
        if (list.isEmpty()) {
            list.add("暂无明显风险信号");
        }
        return list;
    }

    private String suggestion(String rating) {
        switch (rating) {
            case "强烈推荐":
                return "基本面与估值匹配良好，可重点纳入关注池，建议分批建仓。";
            case "推荐":
                return "综合表现较好，可纳入关注池，逢回调分批参与。";
            case "中性":
                return "指标中规中矩，建议结合行业景气度与大盘走势再决策。";
            case "谨慎":
                return "存在一定风险因素，建议谨慎参与或等待更好的价格。";
            default:
                return "当前指标不佳，建议回避，等待基本面改善。";
        }
    }

    private static List<String> splitCodes(String codes) {
        List<String> list = new ArrayList<>();
        if (codes == null) {
            return list;
        }
        for (String part : codes.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list;
    }

    private static double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }
}
