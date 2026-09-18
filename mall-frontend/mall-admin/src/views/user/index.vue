<template>
  <div class="user-page">
    <el-card>
      <div class="filter-bar">
        <el-input v-model="query.keyword" placeholder="搜索用户名/昵称" clearable style="width: 220px" @clear="loadData" @keyup.enter="loadData" />
        <el-button type="primary" @click="loadData">搜索</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : ''">{{ row.role === 'ADMIN' ? '管理员' : '用户' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="toggleStatus(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" />
      </el-table>
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        layout="total, prev, pager, next"
        @change="loadData"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUsers, toggleUserStatus } from '@/api/user'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, keyword: '' })

onMounted(() => loadData())

async function loadData() {
  loading.value = true
  try {
    const res = await getUsers(query)
    tableData.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

async function toggleStatus(row) {
  try {
    await toggleUserStatus(row.id)
    ElMessage.success('状态更新成功')
  } catch { row.status = row.status === 1 ? 0 : 1 }
}
</script>

<style scoped>
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>
