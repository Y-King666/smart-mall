<template>
  <div class="category-page">
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <span>分类管理</span>
          <el-button type="primary" @click="openDialog()">新增分类</el-button>
        </div>
      </template>
      <el-table :data="treeData" row-key="id" :tree-props="{ children: 'children' }" v-loading="loading" default-expand-all>
        <el-table-column prop="name" label="分类名称" min-width="200" />
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确认删除?" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" text size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editId ? '编辑分类' : '新增分类'" width="450px">
      <el-form ref="dialogFormRef" :model="dialogForm" :rules="dialogRules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="dialogForm.name" placeholder="分类名称" />
        </el-form-item>
        <el-form-item label="父分类">
          <el-select v-model="dialogForm.parentId" placeholder="无（顶级分类）" clearable>
            <el-option label="无（顶级分类）" :value="0" />
            <el-option v-for="c in flatList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dialogForm.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getCategories, createCategory, updateCategory, deleteCategory } from '@/api/category'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const saving = ref(false)
const treeData = ref([])
const flatList = ref([])
const dialogVisible = ref(false)
const editId = ref(null)
const dialogFormRef = ref()
const dialogForm = reactive({ name: '', parentId: 0, sort: 0 })
const dialogRules = { name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }] }

onMounted(() => loadData())

async function loadData() {
  loading.value = true
  try {
    const res = await getCategories()
    treeData.value = res
    flatList.value = flattenTree(res)
  } finally { loading.value = false }
}

function flattenTree(tree, result = []) {
  tree.forEach(item => { result.push({ id: item.id, name: item.name }); if (item.children) flattenTree(item.children, result) })
  return result
}

function openDialog(row) {
  if (row) {
    editId.value = row.id
    dialogForm.name = row.name
    dialogForm.parentId = row.parentId
    dialogForm.sort = row.sort
  } else {
    editId.value = null
    dialogForm.name = ''
    dialogForm.parentId = 0
    dialogForm.sort = 0
  }
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await dialogFormRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editId.value) {
      await updateCategory(editId.value, dialogForm)
    } else {
      await createCategory(dialogForm)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally { saving.value = false }
}

async function handleDelete(id) {
  await deleteCategory(id)
  ElMessage.success('删除成功')
  loadData()
}
</script>
