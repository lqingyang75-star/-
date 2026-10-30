package com.example.stockpicker.service;

import com.example.stockpicker.model.PickResult;
import com.example.stockpicker.model.Stock;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 选股服务：用一组简单规则给股票打分并排序。
 *
 * <p>评分规则（雏形版，仅作演示）：
 * <ul>
 *     <li>市盈率 PE 越低越好（0 &lt; PE &lt;= 30 加分）</li>
 *     <li>市净率 PB 越低越好（0 &lt; PB &lt;= 5 加分）</li>
 *     <li>净资产收益率 ROE 越高越好（ROE &gt;= 10 加分）</li>
 * </ul>
 */
@Service
public class StockPickerService {

    private final StockDataProvider dataProvider;

    public StockPickerService(StockDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    /**
     * 按评分从高到低返回候选股票。
     *
     * @param limit 返回条数上限
     * @return 排序后的选股结果
     */
    public List<PickResult> pick(int limit) {
        List<PickResult> results = new ArrayList<>();
        for (Stock stock : dataProvider.allStocks()) {
            results.add(score(stock));
        }
        results.sort(Comparator.comparingDouble(PickResult::score).reversed());
        if (limit > 0 && results.size() > limit) {
            return results.subList(0, limit);
        }
        return results;
    }

    private PickResult score(Stock stock) {
        double score = 0;
        List<String> reasons = new ArrayList<>();

        if (stock.pe() > 0 && stock.pe() <= 30) {
            score += 30 - stock.pe();
            reasons.add("PE 较低(" + stock.pe() + ")");
        }
        if (stock.pb() > 0 && stock.pb() <= 5) {
            score += (5 - stock.pb()) * 5;
            reasons.add("PB 较低(" + stock.pb() + ")");
        }
        if (stock.roe() >= 10) {
            score += stock.roe();
            reasons.add("ROE 较高(" + stock.roe() + "%)");
        }

        String reason = reasons.isEmpty() ? "无明显亮点" : String.join("、", reasons);
        return new PickResult(stock, Math.round(score * 100) / 100.0, reason);
    }
}
