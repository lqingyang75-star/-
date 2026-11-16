import request from './request'

// ---------- 用户 ----------
export function pageUsers(params) {
  return request.get('/system/users', { params })
}

export function userRoleOptions() {
  return request.get('/system/users/role-options')
}

export function createUser(data) {
  return request.post('/system/users', data)
}

export function updateUser(id, data) {
  return request.put(`/system/users/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/system/users/${id}`)
}

export function changeUserStatus(id, status) {
  return request.put(`/system/users/${id}/status`, { status })
}

export function resetUserPassword(id, password) {
  return request.put(`/system/users/${id}/password`, { password })
}

// ---------- 角色 ----------
export function listRoles() {
  return request.get('/system/roles')
}

export function createRole(data) {
  return request.post('/system/roles', data)
}

export function updateRole(id, data) {
  return request.put(`/system/roles/${id}`, data)
}

export function deleteRole(id) {
  return request.delete(`/system/roles/${id}`)
}

export function getRoleMenus(id) {
  return request.get(`/system/roles/${id}/menus`)
}

export function assignRoleMenus(id, menuIds) {
  return request.put(`/system/roles/${id}/menus`, { menuIds })
}

// ---------- 菜单 ----------
export function adminMenuTree() {
  return request.get('/menus/admin-tree')
}

export function createMenu(data) {
  return request.post('/menus', data)
}

export function updateMenu(id, data) {
  return request.put(`/menus/${id}`, data)
}

export function deleteMenu(id) {
  return request.delete(`/menus/${id}`)
}
