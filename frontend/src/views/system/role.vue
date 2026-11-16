<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-button v-permission="'system:role:add'" type="primary" @click="openCreate">新增角色</el-button>
      </div>
      <el-table v-loading="loading" :data="rows" border stripe>
        <el-table-column prop="name" label="角色名称" width="160" />
        <el-table-column prop="code" label="角色标识" width="140" />
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column prop="menuCount" label="权限数量" width="100" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-permission="'system:role:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button
              v-permission="'system:role:assign'"
              link
              type="warning"
              :disabled="row.code === 'ADMIN'"
              @click="openAssign(row)"
            >
              分配权限
            </el-button>
            <el-button
              v-permission="'system:role:delete'"
              link
              type="danger"
              :disabled="row.code === 'ADMIN'"
              @click="onDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑角色' : '新增角色'" width="460px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="角色标识" prop="code">
          <el-input v-model="form.code" maxlength="50" placeholder="如 FINANCE_MANAGER" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="assignVisible" :title="`分配权限 - ${assignRole?.name || ''}`" size="420px">
      <el-tree
        ref="treeRef"
        :data="menuTree"
        :props="{ label: 'name', children: 'children' }"
        node-key="id"
        show-checkbox
        default-expand-all
        class="perm-tree"
      >
        <template #default="{ data }">
          <span>{{ data.name }}</span>
          <el-tag v-if="data.type === 3" size="small" type="info" class="node-tag">按钮</el-tag>
        </template>
      </el-tree>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onAssignSubmit">保存</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminMenuTree,
  assignRoleMenus,
  createRole,
  deleteRole,
  getRoleMenus,
  listRoles,
  updateRole
} from '@/api/system'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const assignVisible = ref(false)
const formRef = ref(null)
const treeRef = ref(null)
const rows = ref([])
const menuTree = ref([])
const assignRole = ref(null)

const form = reactive({ id: null, name: '', code: '', remark: '' })

const rules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入角色标识', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    rows.value = await listRoles()
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, { id: null, name: '', code: '', remark: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, { id: row.id, name: row.name, code: row.code, remark: row.remark })
  dialogVisible.value = true
}

async function onSubmit() {
  if (saving.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = { name: form.name, code: form.code, remark: form.remark }
    if (form.id) {
      await updateRole(form.id, payload)
      ElMessage.success('已保存')
    } else {
      await createRole(payload)
      ElMessage.success('已新增')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function openAssign(row) {
  assignRole.value = row
  assignVisible.value = true
  if (!menuTree.value.length) {
    menuTree.value = await adminMenuTree()
  }
  const checked = await getRoleMenus(row.id)
  await nextTick()
  // 只设置叶子节点，父节点由 el-tree 自动推导半选状态，避免父节点被误判为全选
  const leafIds = collectLeafIds(menuTree.value)
  treeRef.value.setCheckedKeys(checked.filter((id) => leafIds.has(id)))
}

function collectLeafIds(nodes, bucket = new Set()) {
  ;(nodes || []).forEach((node) => {
    if (node.children && node.children.length) {
      collectLeafIds(node.children, bucket)
    } else {
      bucket.add(node.id)
    }
  })
  return bucket
}

async function onAssignSubmit() {
  if (saving.value) return
  saving.value = true
  try {
    const checked = treeRef.value.getCheckedKeys()
    const halfChecked = treeRef.value.getHalfCheckedKeys()
    await assignRoleMenus(assignRole.value.id, [...checked, ...halfChecked])
    ElMessage.success('权限已保存')
    assignVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除角色「${row.name}」吗？`, '确认删除', { type: 'warning' })
  } catch (e) {
    return
  }
  await deleteRole(row.id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}

.perm-tree {
  --el-tree-node-content-height: 30px;
}

.node-tag {
  margin-left: 8px;
  transform: scale(0.85);
}
</style>
