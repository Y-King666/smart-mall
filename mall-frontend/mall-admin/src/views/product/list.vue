<template>
  <div class="product-list">
    <el-card>
      <div class="filter-bar">
        <el-input v-model="query.keyword" placeholder="搜索商品名称" clearable style="width: 200px" @clear="loadData" @keyup.enter="loadData" />
        <el-select v-model="query.categoryId" placeholder="选择分类" clearable @change="loadData" style="width: 180px">
          <el-option v-for="c in categories" :key="c.id" :label="categoryLabel(c)" :value="c.id" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" clearable @change="loadData" style="width: 120px">
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
        <el-button type="primary" @click="loadData">搜索</el-button>
        <el-button type="success" @click="$router.push('/products/edit')">新增商品</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column label="封面" width="80">
          <template #default="{ row }">
            <el-image :src="row.coverImage" style="width: 50px; height: 50px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="price" label="售价" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="salesCount" label="销量" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="toggleStatus(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="$router.push(`/products/edit/${row.id}`)">编辑</el-button>
            <el-popconfirm title="确认删除?" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" text size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="loadData"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getProducts, toggleProductStatus, deleteProduct } from '@/api/product'
import { getCategories } from '@/api/category'
import { categoryLabel, flattenCategories } from '@/utils/category'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const categories = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, keyword: '', categoryId: '', status: '' })

onMounted(() => { loadData(); loadCategories() })

async function loadData() {
  loading.value = true
  try {
    const res = await getProducts(query)
    tableData.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

async function loadCategories() {
  const res = await getCategories()
  // 父子分类都可选：选父分类表示"它下面所有子分类的商品"，由后端展开子树
  categories.value = flattenCategories(res)
}

async function toggleStatus(row) {
  try {
    await toggleProductStatus(row.id)
    ElMessage.success('状态更新成功')
  } catch { row.status = row.status === 1 ? 0 : 1 }
}

async function handleDelete(id) {
  await deleteProduct(id)
  ElMessage.success('删除成功')
  loadData()
}
</script>

<style scoped>
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
</style>
