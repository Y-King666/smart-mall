<template>
  <div class="product-edit">
    <el-card>
      <template #header>{{ isEdit ? '编辑商品' : '新增商品' }}</template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 700px;">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <!-- 只列叶子分类：父分类是分组，商品挂上去后用户端按分类逛不到它 -->
          <el-select v-model="form.categoryId" placeholder="选择分类">
            <el-option v-for="c in leafCategories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="售价" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="原价" prop="originalPrice">
          <el-input-number v-model="form.originalPrice" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" />
        </el-form-item>
        <el-form-item label="封面图" prop="coverImage">
          <el-upload
            class="cover-uploader"
            action=""
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="handleUpload"
            accept="image/jpeg,image/png,image/gif,image/webp"
          >
            <!-- 已上传图片预览 -->
            <el-image
              v-if="form.coverImage"
              :src="form.coverImage"
              fit="cover"
              class="cover-preview"
            />
            <!-- 未上传时的占位区域 -->
            <div v-else class="cover-placeholder">
              <el-icon :size="40"><Plus /></el-icon>
              <span>点击上传封面图</span>
            </div>
          </el-upload>
          <div class="upload-tip">支持 JPG/PNG/GIF/WebP 格式，最大 5MB</div>
        </el-form-item>
        <el-form-item label="简要描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
        </el-form-item>
        <el-form-item label="商品详情" prop="detail">
          <el-input v-model="form.detail" type="textarea" :rows="8" placeholder="支持HTML富文本" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
          <el-button @click="$router.back()">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createProduct, updateProduct, getProduct } from '@/api/product'
import { getCategories } from '@/api/category'
import { flattenCategories } from '@/utils/category'
import { uploadImage } from '@/api/upload'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const saving = ref(false)
const isEdit = computed(() => !!route.params.id)
const leafCategories = ref([])

const form = reactive({
  name: '', categoryId: '', price: 0, originalPrice: 0, stock: 0,
  coverImage: '', description: '', detail: '', status: 1
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }]
}

onMounted(async () => {
  const catRes = await getCategories()
  leafCategories.value = flattenCategories(catRes).filter((c) => c.isLeaf)
  if (isEdit.value) {
    const res = await getProduct(route.params.id)
    Object.assign(form, res)
  }
})

/** 上传前校验：文件类型和大小 */
function beforeUpload(file) {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('仅支持 JPG/PNG/GIF/WebP 格式')
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

/** 自定义上传处理 */
async function handleUpload({ file }) {
  try {
    const url = await uploadImage(file)
    form.coverImage = url
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error('上传失败')
  }
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit.value) {
      await updateProduct(route.params.id, form)
    } else {
      await createProduct(form)
    }
    ElMessage.success('保存成功')
    router.push('/products')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally { saving.value = false }
}
</script>

<style scoped>
/* 封面图上传区域样式 */
.cover-uploader :deep(.el-upload) {
  width: 180px;
  height: 180px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.3s;
}
.cover-uploader :deep(.el-upload:hover) {
  border-color: #409eff;
}
.cover-preview {
  width: 180px;
  height: 180px;
  display: block;
}
.cover-placeholder {
  width: 180px;
  height: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
  gap: 8px;
  font-size: 14px;
}
.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
