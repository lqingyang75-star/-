package com.example.stockpicker.stock.service;

import com.example.stockpicker.common.BizException;
import com.example.stockpicker.stock.entity.WatchStock;
import com.example.stockpicker.stock.model.PickResult;
import com.example.stockpicker.stock.model.Stock;
import com.example.stockpicker.stock.repository.WatchStockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 自选股服务：按用户维护关注列表，查询时实时拉取行情并复用选股打分。
 */
@Service
public class WatchStockService {

    /** 单用户自选上限，避免一次拉取过多行情拖慢接口 */
    private static final int MAX_SIZE = 50;

    private static final Pattern CODE_PATTERN = Pattern.compile("\\d{6}");

    private final WatchStockRepository watchStockRepository;
    private final RealTimeStockDataProvider realTimeProvider;
    private final StockPickerService stockPickerService;

    public WatchStockService(WatchStockRepository watchStockRepository,
                             RealTimeStockDataProvider realTimeProvider,
                             StockPickerService stockPickerService) {
        this.watchStockRepository = watchStockRepository;
        this.realTimeProvider = realTimeProvider;
        this.stockPickerService = stockPickerService;
    }

    /**
     * 自选列表（含实时行情与均衡型评分），按加入顺序返回。
     */
    @Transactional(readOnly = true)
    public List<PickResult> list(Long userId) {
        List<WatchStock> watched = watchStockRepository.findByUserIdOrderByIdAsc(userId);
        if (watched.isEmpty()) {
            return List.of();
        }
        String codes = watched.stream().map(WatchStock::getStockCode).reduce((a, b) -> a + "," + b).orElse("");
        // pick 会按评分排序，这里按加入顺序重排，保持列表稳定
        Map<String, PickResult> byCode = new LinkedHashMap<>();
        stockPickerService.pick(0, codes).forEach(item -> byCode.put(item.stock().code(), item));
        List<PickResult> result = new ArrayList<>();
        for (WatchStock stock : watched) {
            PickResult item = byCode.get(stock.getStockCode());
            if (item != null) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * 当前用户的自选代码列表，供行情页回显收藏状态。
     */
    @Transactional(readOnly = true)
    public List<String> codes(Long userId) {
        return watchStockRepository.findByUserIdOrderByIdAsc(userId).stream()
                .map(WatchStock::getStockCode)
                .toList();
    }

    @Transactional
    public void add(Long userId, String code) {
        if (!StringUtils.hasText(code) || !CODE_PATTERN.matcher(code.trim()).matches()) {
            throw new BizException(400, "请输入 6 位数字股票代码");
        }
        String stockCode = code.trim();
        if (watchStockRepository.existsByUserIdAndStockCode(userId, stockCode)) {
            throw new BizException(400, "该股票已在自选列表中");
        }
        if (watchStockRepository.countByUserId(userId) >= MAX_SIZE) {
            throw new BizException(400, "自选列表最多 " + MAX_SIZE + " 只股票");
        }
        List<Stock> stocks = realTimeProvider.fetchByCodes(List.of(stockCode));
        if (stocks.isEmpty()) {
            throw new BizException(400, "未获取到该股票的行情，请检查代码是否正确");
        }
        WatchStock watchStock = new WatchStock();
        watchStock.setUserId(userId);
        watchStock.setStockCode(stockCode);
        watchStock.setStockName(stocks.get(0).name());
        watchStockRepository.save(watchStock);
    }

    @Transactional
    public void remove(Long userId, String code) {
        WatchStock watchStock = watchStockRepository.findByUserIdAndStockCode(userId, code)
                .orElseThrow(() -> new BizException(400, "该股票不在自选列表中"));
        watchStockRepository.delete(watchStock);
    }
}
