import request from './request'

// 行情查询：不传 codes 时返回内置股票池，传 codes 时按实时行情返回
export function pickStocks(params) {
  return request.get('/stocks/pick', { params }).then((data) => data || [])
}

// 个股诊断：生成单只股票的分析报告
export function analyzeStock(code) {
  return request.get('/stocks/analyze', { params: { code } })
}

// 多策略对比：对同一批股票分别按价值/成长/均衡策略打分
export function compareStocks(codes) {
  return request.get('/stocks/compare', { params: { codes } })
}

// ---------- 自选股 ----------
export function listWatchlist() {
  return request.get('/stocks/watchlist').then((data) => data || [])
}

export function listWatchCodes() {
  return request.get('/stocks/watchlist/codes').then((data) => data || [])
}

export function addWatch(code) {
  return request.post('/stocks/watchlist', { code })
}

export function removeWatch(code) {
  return request.delete(`/stocks/watchlist/${code}`)
}
