import { defineStore } from 'pinia'
import { login as loginApi, me as meApi } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null,
    menuTree: [],
    routesLoaded: false,
    // 登录后默认落地的页面，由菜单树第一个可访问菜单决定
    firstPage: ''
  }),
  getters: {
    perms: (state) => state.userInfo?.perms || []
  },
  actions: {
    hasPerm(perm) {
      return this.perms.includes(perm)
    },
    async login(form) {
      const data = await loginApi(form)
      this.token = data.token
      this.userInfo = data.userInfo
      localStorage.setItem('token', data.token)
    },
    async fetchMe() {
      this.userInfo = await meApi()
    },
    logout() {
      this.token = ''
      this.userInfo = null
      this.menuTree = []
      this.routesLoaded = false
      this.firstPage = ''
      localStorage.removeItem('token')
    }
  }
})
