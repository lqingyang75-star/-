<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">个人投资记账</div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#1f2937"
        text-color="#cbd5e1"
        active-text-color="#ffffff"
        class="menu"
      >
        <SidebarItem v-for="item in store.menuTree" :key="item.id" :menu="item" />
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span class="page-title">{{ route.meta.title || '' }}</span>
        <el-dropdown @command="onCommand">
          <span class="user">
            {{ store.userInfo?.realName || store.userInfo?.username || '未登录' }}
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import SidebarItem from './SidebarItem.vue'

const store = useUserStore()
const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)

function onCommand(command) {
  if (command === 'logout') {
    store.logout()
    router.replace('/login')
  }
}
</script>

<style scoped>
.layout {
  height: 100%;
}

.aside {
  background: #1f2937;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 56px;
  line-height: 56px;
  text-align: center;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.menu {
  border-right: none;
  flex: 1;
}

.header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}

.page-title {
  font-size: 15px;
  font-weight: 600;
}

.user {
  cursor: pointer;
  color: #409eff;
  font-size: 14px;
}

.main {
  background: #f5f7fa;
  padding: 16px;
}
</style>
