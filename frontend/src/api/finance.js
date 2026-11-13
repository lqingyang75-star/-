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
