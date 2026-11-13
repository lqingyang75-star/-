<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-radio-group v-model="activeType" @change="load">
          <el-radio-button :label="1">收入分类</el-radio-button>
          <el-radio-button :label="2">支出分类</el-radio-button>
        </el-radio-group>
        <el-button v-permission="'fin:category:add'" type="primary" @click="openCreate">新增分类</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" border stripe>
        <el-table-column prop="name" label="分类名称" min-width="180" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'success' : 'danger'" size="small">
              {{ row.type === 1 ? '收入' : '支出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button v-permission="'fin:category:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-permission="'fin:category:delete'" link type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="420px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" maxlength="50" placeholder="如 餐饮、工资" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :label="1">收入</el-radio>
            <el-radio :label="2">支出</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :step="1" style="width: 140px" />
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createCategory, deleteCategory, listCategories, updateCategory } from '@/api/finance'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const rows = ref([])
const activeType = ref(1)

const form = reactive({ id: null, name: '', type: 1, sort: 0 })

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

async function load() {
  loading.value = true
  try {
    rows.value = await listCategories({ type: activeType.value })
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, { id: null, name: '', type: activeType.value, sort: 0 })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, { id: row.id, name: row.name, type: row.type, sort: row.sort })
  dialogVisible.value = true
}

async function onSubmit() {
  if (saving.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = { name: form.name, type: form.type, sort: form.sort }
    if (form.id) {
      await updateCategory(form.id, payload)
      ElMessage.success('已保存')
    } else {
      await createCategory(payload)
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
    await ElMessageBox.confirm(`确定删除分类「${row.name}」吗？`, '确认删除', { type: 'warning' })
  } catch (e) {
    return
  }
  await deleteCategory(row.id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}
</style>
