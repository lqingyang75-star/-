<template>
  <div class="login-page">
    <el-card class="login-card" shadow="always">
      <h2>个人投资记账系统</h2>
      <p class="sub">RBAC 权限管理 · 收支与交易记账</p>

      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent>
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" size="large" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            @keyup.enter="onSubmit"
          />
        </el-form-item>
        <el-button type="primary" size="large" class="submit" :loading="loading" @click="onSubmit">
          登 录
        </el-button>
      </el-form>

      <p class="hint">初始账号 admin / admin123</p>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const store = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: 'admin123'
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onSubmit() {
  if (loading.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await store.login({ username: form.username, password: form.password })
    ElMessage.success('登录成功')
    // 路由守卫会加载菜单并注册动态路由
    await router.replace('/')
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f2937 0%, #0f766e 100%);
}

.login-card {
  width: 380px;
  padding: 8px 12px;
  border-radius: 12px;
}

h2 {
  margin: 8px 0 4px;
  font-size: 20px;
  text-align: center;
}

.sub {
  margin: 0 0 24px;
  font-size: 13px;
  color: #909399;
  text-align: center;
}

.submit {
  width: 100%;
}

.hint {
  margin: 16px 0 4px;
  font-size: 12px;
  color: #c0c4cc;
  text-align: center;
}
</style>
