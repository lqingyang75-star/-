<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-label">当前登录人</div>
          <div class="stat-value">{{ store.userInfo?.realName || store.userInfo?.username }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-label">拥有权限码</div>
          <div class="stat-value">{{ store.perms.length }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-label">可见菜单</div>
          <div class="stat-value">{{ menuCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="tip-card">
      <p>阶段一已完成：JWT 登录、RBAC 权限模型、动态菜单与路由、软件结构按业务域分包。</p>
      <p>接下来的阶段二为财务收支模块（记录、分类、筛选、统计），阶段四补充首页图表、Excel 导出与操作日志。</p>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/user'

const store = useUserStore()

const menuCount = computed(() => {
  let count = 0
  const walk = (nodes) => {
    (nodes || []).forEach((node) => {
      if (node.type === 2) count += 1
      if (node.children && node.children.length) walk(node.children)
    })
  }
  walk(store.menuTree)
  return count
})
</script>

<style scoped>
.stat-label {
  font-size: 13px;
  color: #909399;
}

.stat-value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 600;
}

.tip-card {
  margin-top: 16px;
  line-height: 1.9;
  color: #606266;
  font-size: 14px;
}
</style>
