<template>
  <div>
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="关键字">
          <el-input
            v-model="filters.keyword"
            placeholder="账号或姓名"
            clearable
            style="width: 180px"
            @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <div class="toolbar">
        <el-button v-permission="'system:user:add'" type="primary" @click="openCreate">新增用户</el-button>
      </div>
      <el-table v-loading="loading" :data="rows" border stripe>
        <el-table-column prop="username" label="账号" width="140" />
        <el-table-column prop="realName" label="姓名" width="140" />
        <el-table-column label="角色" min-width="180">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role.id" size="small" class="role-tag">
              {{ role.name }}
            </el-tag>
            <span v-if="!row.roles.length" class="muted">未分配</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              :disabled="row.id === currentUserId"
              @change="(val) => onToggleStatus(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">{{ (row.createdAt || '').replace('T', ' ') }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-permission="'system:user:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-permission="'system:user:reset'" link type="warning" @click="onResetPassword(row)">重置密码</el-button>
            <el-button v-permission="'system:user:delete'" link type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="filters.page"
        v-model:page-size="filters.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="pager"
        @current-change="load"
        @size-change="onSizeChange"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" maxlength="50" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="初始密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" maxlength="50" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option v-for="role in roleOptions" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  changeUserStatus,
  createUser,
  deleteUser,
  pageUsers,
  resetUserPassword,
  updateUser,
  userRoleOptions
} from '@/api/system'
import { useUserStore } from '@/store/user'

const store = useUserStore()
const currentUserId = computed(() => store.userInfo?.id)

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const rows = ref([])
const total = ref(0)
const roleOptions = ref([])

const filters = reactive({ keyword: '', status: null, page: 1, size: 10 })
const form = reactive(emptyForm())

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }]
}

function emptyForm() {
  return { id: null, username: '', password: '', realName: '', status: 1, roleIds: [] }
}

async function load() {
  loading.value = true
  try {
    const data = await pageUsers({
      keyword: filters.keyword || undefined,
      status: filters.status === 0 || filters.status === 1 ? filters.status : undefined,
      page: filters.page,
      size: filters.size
    })
    rows.value = data.items || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function onSearch() {
  filters.page = 1
  load()
}

function onReset() {
  filters.keyword = ''
  filters.status = null
  filters.page = 1
  load()
}

function onSizeChange() {
  filters.page = 1
  load()
}

function openCreate() {
  Object.assign(form, emptyForm())
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    username: row.username,
    password: '',
    realName: row.realName,
    status: row.status,
    roleIds: row.roles.map((r) => r.id)
  })
  dialogVisible.value = true
}

async function onSubmit() {
  if (saving.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = {
      username: form.username,
      realName: form.realName,
      status: form.status,
      roleIds: form.roleIds
    }
    if (form.id) {
      await updateUser(form.id, payload)
      ElMessage.success('已保存')
    } else {
      await createUser({ ...payload, password: form.password })
      ElMessage.success('已新增')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function onToggleStatus(row, val) {
  const next = val ? 1 : 0
  try {
    await changeUserStatus(row.id, next)
    row.status = next
    ElMessage.success(next === 1 ? '已启用' : '已禁用')
  } catch (e) {
    row.status = next === 1 ? 0 : 1
  }
}

async function onResetPassword(row) {
  try {
    const { value } = await ElMessageBox.prompt(`为「${row.username}」设置新密码`, '重置密码', {
      inputType: 'password',
      inputPattern: /^.{6,}$/,
      inputErrorMessage: '密码至少 6 位'
    })
    await resetUserPassword(row.id, value)
    ElMessage.success('密码已重置')
  } catch (e) {
    /* 取消或失败均不提示 */
  }
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除用户「${row.username}」吗？`, '确认删除', { type: 'warning' })
  } catch (e) {
    return
  }
  await deleteUser(row.id)
  ElMessage.success('已删除')
  await load()
}

onMounted(async () => {
  roleOptions.value = await userRoleOptions()
  await load()
})
</script>

<style scoped>
.filter-card {
  margin-bottom: 16px;
}

.filter-card :deep(.el-form-item) {
  margin-bottom: 0;
}

.toolbar {
  margin-bottom: 12px;
}

.pager {
  margin-top: 14px;
  justify-content: flex-end;
}

.role-tag {
  margin-right: 6px;
}

.muted {
  color: #c0c4cc;
}
</style>
