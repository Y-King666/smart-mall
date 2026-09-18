<!--
  注册页

  与登录页共用同一套版式（左品牌陈述 / 右表单），保持视觉连贯。
  注册成功后角色为普通用户，跳回登录页让用户手动登录。
-->
<template>
  <div class="auth">
    <aside class="auth__brand">
      <div class="auth__brand-inner">
        <img src="/logo.png" alt="严选商城" class="auth__logo" />
        <h1 class="mp-serif auth__slogan">以东方极简美学<br />臻选日常良品</h1>
        <p class="auth__sub">器物有序，取舍有度</p>
      </div>
    </aside>

    <main class="auth__main">
      <div class="auth__card">
        <p class="auth__eyebrow">JOIN US</p>
        <h2 class="mp-serif auth__title">注册</h2>
        <p class="auth__hint">注册一个账号，开始甄选你的日常良品</p>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleSubmit">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" size="large" placeholder="用于登录，注册后不可修改" />
          </el-form-item>
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" size="large" placeholder="选填，不填则与用户名相同" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              size="large"
              placeholder="请输入密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              size="large"
              placeholder="请再次输入密码"
              show-password
              @keyup.enter="handleSubmit"
            />
          </el-form-item>
          <button class="mp-btn auth__submit" type="button" :disabled="loading" @click="handleSubmit">
            {{ loading ? '注册中…' : '注 册' }}
          </button>
        </el-form>

        <p class="auth__switch">
          已有账号？<router-link to="/login" class="auth__link">前往登录</router-link>
        </p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register as registerApi } from '@/api/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', nickname: '', password: '', confirmPassword: '' })

/** 两次输入的密码必须一致 */
function validateConfirm(rule, value, callback) {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await registerApi({
      username: form.username,
      password: form.password,
      nickname: form.nickname
    })
    ElMessage.success('注册成功，请登录')
    router.replace('/login')
  } catch (e) {
    ElMessage.error(e.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<!-- 版式样式在 styles/auth.css 中由登录/注册页共享，此处无需重复定义 -->
