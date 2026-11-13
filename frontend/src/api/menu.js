import request from './request'

/** 当前登录人可见菜单树 */
export function fetchRoutes() {
  return request.get('/menus/routes')
}

/** 全量菜单树（菜单管理用） */
export function fetchMenuTree() {
  return request.get('/menus/tree')
}
