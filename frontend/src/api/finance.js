import request from './request'

// ---------- 分类 ----------
export function listCategories(params) {
  return request.get('/finance/categories', { params })
}

export function createCategory(data) {
  return request.post('/finance/categories', data)
}

export function updateCategory(id, data) {
  return request.put(`/finance/categories/${id}`, data)
}

export function deleteCategory(id) {
  return request.delete(`/finance/categories/${id}`)
}

// ---------- 收支记录 ----------
export function pageRecords(params) {
  return request.get('/finance/records', { params })
}

export function statRecords(params) {
  return request.get('/finance/records/stat', { params })
}

export function createRecord(data) {
  return request.post('/finance/records', data)
}

export function updateRecord(id, data) {
  return request.put(`/finance/records/${id}`, data)
}

export function deleteRecord(id) {
  return request.delete(`/finance/records/${id}`)
}

export function batchDeleteRecords(ids) {
  return request.post('/finance/records/batch-delete', { ids })
}

// 导出 Excel，返回二进制内容
export function exportRecords(params) {
  return request.get('/finance/records/export', { params, responseType: 'blob' })
}

// ---------- 看板图表 ----------
export function monthlyTrend(months = 6) {
  return request.get('/finance/records/chart/monthly-trend', { params: { months } })
}

export function weeklyBar() {
  return request.get('/finance/records/chart/weekly-bar')
}

export function categoryPie(params) {
  return request.get('/finance/records/chart/category-pie', { params })
}
