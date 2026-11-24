import request from './request'

// 行情查询：不传 codes 时返回内置股票池，传 codes 时按实时行情返回
export function pickStocks(params) {
  return request.get('/stocks/pick', { params }).then((data) => data || [])
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
