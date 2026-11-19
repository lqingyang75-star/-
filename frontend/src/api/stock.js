import request from './request'

// 行情查询：不传 codes 时返回内置股票池，传 codes 时按实时行情返回
export function pickStocks(params) {
  return request.get('/stocks/pick', { params }).then((data) => data || [])
}
