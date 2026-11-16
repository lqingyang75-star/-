<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-button v-permission="'system:menu:add'" type="primary" @click="openCreate()">新增菜单</el-button>
        <el-button @click="load">刷新</el-button>
      </div>
      <el-table
        v-loading="loading"
        :data="rows"
        row-key="id"
        border
        default-expand-all
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="name" label="菜单名称" min-width="180" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.type)" size="small">{{ typeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" min-width="140" show-overflow-tooltip />
        <el-table-column prop="component" label="组件路径" min-width="160" show-overflow-tooltip />
        <el-table-column prop="perms" label="权限标识" min-width="160" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="70" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button v-permission="'system:menu:add'" link type="primary" :disabled="row.type === 3" @click="openCreate(row)">
              新增下级
            </el-button>
            <el-button v-permission="'system:menu:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-permission="'system:menu:delete'" link type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑菜单' : '新增菜单'" width="540px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            :props="{ label: 'name', children: 'children' }"
            node-key="id"
            check-strictly
            default-expand-all
            placeholder="不选则为顶级菜单"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="form.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="菜单类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio-button :value="1">目录</el-radio-button>
            <el-radio-button :value="2">菜单</el-radio-button>
            <el-radio-button :value="3">按钮</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.type !== 3" label="路由路径" prop="path">
          <el-input v-model="form.path" maxlength="100" placeholder="如 /system/user" />
        </el-form-item>
        <el-form-item v-if="form.type === 2" label="组件路径" prop="component">
          <el-input v-model="form.component" maxlength="120" placeholder="如 system/user" />
        </el-form-item>
        <el-form-item v-if="form.type === 3" label="权限标识" prop="perms">
          <el-input v-model="form.perms" maxlength="100" placeholder="如 system:user:add" />
        </el-form-item>
        <el-form-item v-if="form.type !== 3" label="图标">
          <el-input v-model="form.icon" maxlength="50" placeholder="Element Plus 图标名，可留空" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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
import { adminMenuTree, createMenu, deleteMenu, updateMenu } from '@/api/system'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const rows = ref([])

const form = reactive({
  id: null,
  parentId: 0,
  name: '',
  type: 2,
  path: '',
  component: '',
  perms: '',
  icon: '',
  sort: 0,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  path: [{ required: true, message: '菜单必须填写路由路径', trigger: 'blur' }],
  component: [{ required: true, message: '菜单必须填写组件路径', trigger: 'blur' }],
  perms: [{ required: true, message: '按钮必须填写权限标识', trigger: 'blur' }]
}

// 上级候选：排除按钮，编辑时再排除自己及所有子孙，避免形成环
const parentOptions = computed(() => {
  const skip = new Set()
  if (form.id) {
    const walk = (nodes) => {
      ;(nodes || []).forEach((node) => {
        if (node.id === form.id) {
          collectIds(node, skip)
        } else {
          walk(node.children)
        }
      })
    }
    walk(rows.value)
  }
  const build = (nodes) =>
    (nodes || [])
      .filter((node) => node.type !== 3 && !skip.has(node.id))
      .map((node) => ({ id: node.id, name: node.name, children: build(node.children) }))
  return [{ id: 0, name: '顶级菜单', children: build(rows.value) }]
})

function collectIds(node, bucket) {
  bucket.add(node.id)
  ;(node.children || []).forEach((child) => collectIds(child, bucket))
}

function typeText(type) {
  return { 1: '目录', 2: '菜单', 3: '按钮' }[type] || '未知'
}

function typeTag(type) {
  return { 1: 'warning', 2: 'primary', 3: 'info' }[type] || 'info'
}

async function load() {
  loading.value = true
  try {
    rows.value = await adminMenuTree()
  } finally {
    loading.value = false
  }
}

function resetForm(parentId) {
  Object.assign(form, {
    id: null,
    parentId: parentId || 0,
    name: '',
    type: parentId ? 2 : 1,
    path: '',
    component: '',
    perms: '',
    icon: '',
    sort: 0,
    status: 1
  })
}

function openCreate(row) {
  resetForm(row ? row.id : 0)
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    parentId: row.parentId || 0,
    name: row.name,
    type: row.type,
    path: row.path || '',
    component: row.component || '',
    perms: row.perms || '',
    icon: row.icon || '',
    sort: row.sort ?? 0,
    status: row.status ?? 1
  })
  dialogVisible.value = true
}

async function onSubmit() {
  if (saving.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    // 与类型无关的字段置空，避免脏数据
    const payload = {
      parentId: form.parentId || 0,
      name: form.name.trim(),
      type: form.type,
      path: form.type === 3 ? null : form.path || null,
      component: form.type === 2 ? form.component || null : null,
      perms: form.type === 3 ? form.perms || null : null,
      icon: form.type === 3 ? null : form.icon || null,
      sort: form.sort ?? 0,
      status: form.status ?? 1
    }
    if (form.id) {
      await updateMenu(form.id, payload)
      ElMessage.success('已保存')
    } else {
      await createMenu(payload)
      ElMessage.success('已新增')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.name}」吗？`, '确认删除', { type: 'warning' })
  } catch (e) {
    return
  }
  await deleteMenu(row.id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
</style>
