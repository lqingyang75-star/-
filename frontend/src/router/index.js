import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import { fetchRoutes } from '@/api/menu'

// 所有页面组件，供菜单里的 component 字符串动态匹配
const viewModules = import.meta.glob('../views/**/*.vue')

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/index.vue'),
      meta: { public: true }
    },
    {
      path: '/',
      name: 'layout',
      component: () => import('@/layout/index.vue'),
      children: []
    },
    {
      // 兜底用 404 页面，不能重定向到 '/'，否则会与 '/' 相互重定向形成死循环
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFound.vue')
    }
  ]
})

function resolveComponent(component) {
  const fallback = viewModules['../views/UnderConstruction.vue']
  if (!component) return fallback
  return viewModules[`../views/${component}.vue`] || fallback
}

/**
 * 按后端菜单树注册动态路由。只有 type=2 的菜单才对应页面。
 * 返回已注册的页面列表，顺序即菜单顺序，用于确定登录后的首页。
 */
export function registerMenuRoutes(tree) {
  const pages = []
  const walk = (nodes) => {
    (nodes || []).forEach((node) => {
      if (node.type === 2 && node.path) {
        pages.push(node)
      }
      if (node.children && node.children.length) {
        walk(node.children)
      }
    })
  }
  walk(tree)
  pages.forEach((menu) => {
    router.addRoute('layout', {
      path: menu.path,
      name: `menu-${menu.id}`,
      component: resolveComponent(menu.component),
      meta: { title: menu.name }
    })
  })
  return pages
}

router.beforeEach(async (to) => {
  const store = useUserStore()
  if (to.meta.public) {
    return true
  }
  if (!store.token) {
    return { path: '/login' }
  }

  if (!store.routesLoaded) {
    try {
      if (!store.userInfo) {
        await store.fetchMe()
      }
      const tree = await fetchRoutes()
      store.menuTree = tree || []
      const pages = registerMenuRoutes(store.menuTree)
      store.firstPage = pages.length ? pages[0].path : ''
      store.routesLoaded = true
    } catch (e) {
      store.logout()
      return { path: '/login' }
    }
    // 路由是刚刚注册的，需要按新路由表重新解析目标地址
    const target = to.path === '/' ? store.firstPage : to.path
    return target && target !== to.path ? { path: target, replace: true } : { path: to.path, replace: true }
  }

  // 已加载过路由：访问根路径或未匹配地址时，落到第一个可见菜单
  if (to.path === '/' || to.name === 'not-found') {
    if (store.firstPage && store.firstPage !== to.path) {
      return { path: store.firstPage, replace: true }
    }
  }
  return true
})

export default router
