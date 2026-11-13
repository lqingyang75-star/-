<template>
  <el-result icon="warning" title="页面不存在" sub-title="请从左侧菜单选择要访问的页面">
    <template #extra>
      <el-button type="primary" @click="goHome">回到首页</el-button>
    </template>
  </el-result>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const store = useUserStore()

function goHome() {
  router.replace(store.firstPage || '/')
}
</script>
