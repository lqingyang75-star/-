package com.example.stockpicker.stock.service;

import com.example.stockpicker.stock.model.Stock;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 实时行情数据源。
 *
 * <p>行情（最新价/PE/PB/涨跌幅）来自腾讯行情接口
 * {@code http://qt.gtimg.cn/q=sh600519,sz000651...}，ROE（加权净资产收益率）
 * 来自东方财富 F10 财报接口。ROE 为季度财报指标（非实时频率），与行情分开获取。
 *
 * <p>数据链路：前端传入股票代码 → 拉取各代码实时行情 + 最新 ROE → 组装为 Stock 供打分。
 */
@Component
public class RealTimeStockDataProvider implements StockDataProvider {

    /** 判读代码市场：6 开头的沪市用 sh，其余（0/3 深市、688 等）用 sz。 */
    private static final Pattern CODE_PATTERN = Pattern.compile("(\\d{6})");

    private static final String TENCENT_URL = "http://qt.gtimg.cn/q=";
    private static final String ROE_URL =
            "https://datacenter.eastmoney.com/securities/api/data/get"
            + "?type=RPT_F10_FINANCE_MAINFINADATA"
            + "&sty=APP_F10_MAINFINADATA"
            + "&filter=(SECUCODE=%22{SECUCODE}%22)&p=1&ps=1";

    /** 腾讯行情响应体每个字段以 ~ 分隔（v_xxx="..."）。 */
    private static final Pattern TX_BODY =
            Pattern.compile("v_(\\w+)=\"(.*)\"");
    private static final Pattern ROE_PATTERN =
            Pattern.compile("\"ROEJQ\":(-?[\\d.]+)");

    private static final int TIMEOUT_MS = 5000;

    /** ROE 财务指标并发拉取的线程池与整体超时。 */
    private static final ExecutorService ROE_POOL = Executors.newFixedThreadPool(8);
    private static final int ROE_TIMEOUT_SECONDS = 20;

    private static final Charset GB18030 =
            Charset.forName("GB18030");

    public RealTimeStockDataProvider() {
    }

    /**
     * 批量拉取指定股票代码的实时行情。
     *
     * @param codes 6 位股票代码列表
     * @return 行情与 ROE 完整的实时股票列表（无 ROE 时用 0 参与打分）
     */
    public List<Stock> fetchByCodes(List<String> codes) {
        List<Stock> result = new ArrayList<>();
        if (codes == null || codes.isEmpty()) {
            return result;
        }
        try {
            Map<String, String> quotes = fetchQuotes(codes);
            Map<String, Double> roeMap = fetchRoe(codes);
            for (Map.Entry<String, String> entry : quotes.entrySet()) {
                String key = entry.getKey();
                String body = entry.getValue();
                String[] f = body.split("~", -1);
                String code6 = get(f, 2);
                if (code6.isEmpty()) {
                    code6 = key.replaceAll("(sh|sz|bj)", "");
                }
                // 腾讯对无效代码返回 v_pv_none_match=""，非 6 位代码一律跳过
                if (!code6.matches("\\d{6}")) {
                    continue;
                }
                Stock stock = new Stock(
                        code6,
                        get(f, 1),
                        parseDouble(get(f, 3)),
                        parseDouble(get(f, 39)),
                        parseDouble(get(f, 46)),
                        roeMap.getOrDefault(code6, 0.0),
                        parseDouble(get(f, 32))
                );
                result.add(stock);
            }
        } catch (IOException e) {
            throw new IllegalStateException("实时行情获取失败: " + e.getMessage(), e);
        }
        return result;
    }

    /** 兼容旧签名，无参数时回退到内置样例数据。 */
    @Override
    public List<Stock> allStocks() {
        return new SampleStockDataProvider().allStocks();
    }

    // ---------- 腾讯行情 ----------

    private Map<String, String> fetchQuotes(List<String> codes) throws IOException {
        StringBuilder query = new StringBuilder();
        for (String code : codes) {
            String m = normalizeCode(code);
            if (m != null) {
                query.append(m).append(',');
            }
        }
        Map<String, String> map = new LinkedHashMap<>();
        if (query.length() == 0) {
            return map;
        }
        String body = httpGet(TENCENT_URL + query, null);
        Matcher matcher = TX_BODY.matcher(body);
        while (matcher.find()) {
            map.put(matcher.group(1), matcher.group(2));
        }
        return map;
    }

    // ---------- 东财 ROE ----------

    private Map<String, Double> fetchRoe(List<String> codes) {
        Map<String, Double> result = new ConcurrentHashMap<>();
        if (codes == null || codes.isEmpty()) {
            return result;
        }
        // 批量接口不稳定，逐只查询；并发拉取以缩短选股扫描耗时，一只失败不影响其余
        List<CompletableFuture<Void>> tasks = new ArrayList<>();
        for (String code : codes) {
            tasks.add(CompletableFuture.runAsync(() -> {
                String secu = toSecuCode(code);
                if (secu == null) {
                    return;
                }
                try {
                    String url = ROE_URL.replace("{SECUCODE}",
                            URLEncoder.encode(secu, StandardCharsets.UTF_8));
                    String body = httpGet(url, null);
                    Matcher m = ROE_PATTERN.matcher(body);
                    if (m.find()) {
                        result.put(code, Double.parseDouble(m.group(1)));
                    }
                } catch (IOException | NumberFormatException ignored) {
                    // ROE 缺失不影响行情打分
                }
            }, ROE_POOL));
        }
        try {
            CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]))
                    .get(ROE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception ignored) {
            // 整体超时则使用已取到的部分结果
        }
        return result;
    }

    // ---------- 工具 ----------

    private String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        Matcher matcher = CODE_PATTERN.matcher(code.trim());
        if (!matcher.find()) {
            return null;
        }
        String c = matcher.group(1);
        return (c.startsWith("6") ? "sh" : "sz") + c;
    }

    private String toSecuCode(String code) {
        if (code == null) {
            return null;
        }
        Matcher matcher = CODE_PATTERN.matcher(code.trim());
        if (!matcher.find()) {
            return null;
        }
        String c = matcher.group(1);
        return c + (c.startsWith("6") ? ".SH" : ".SZ");
    }

    private static String get(String[] arr, int idx) {
        if (arr == null || idx < 0 || idx >= arr.length) {
            return "";
        }
        return arr[idx] == null ? "" : arr[idx].trim();
    }

    private static double parseDouble(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private static String httpGet(String urlStr, String referer) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            if (referer != null) {
                conn.setRequestProperty("Referer", referer);
            }
            int status = conn.getResponseCode();
            if (status != 200) {
                throw new IOException("HTTP " + status);
            }
            String charset = "GB18030";
            String contentType = conn.getContentType();
            if (contentType != null && contentType.toLowerCase().contains("charset=utf-8")) {
                charset = "UTF-8";
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(),
                            Charset.forName(charset)))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }
}
