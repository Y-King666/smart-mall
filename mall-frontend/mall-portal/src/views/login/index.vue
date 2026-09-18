<!--
  登录页

  设计稿没有登录页，本页按设计规范「东方编辑式极简」自行设计：
  左侧品牌陈述（衬线标题 + 暖色底），右侧表单卡片，窄屏时收敛为单栏。
-->
<template>
  <div class="auth">
    <!-- 品牌侧 -->
    <aside class="auth__brand">
      <div class="auth__brand-inner">
        <img src="/logo.png" alt="严选商城" class="auth__logo" />
        <h1 class="mp-serif auth__slogan">以东方极简美学<br />臻选日常良品</h1>
        <p class="auth__sub">器物有序，取舍有度</p>
      </div>
    </aside>

    <!-- 表单侧 -->
    <main class="auth__main">
      <div class="auth__card">
        <p class="auth__eyebrow">WELCOME BACK</p>
        <h2 class="mp-serif auth__title">登录</h2>
        <p class="auth__hint">登录后可加入购物车、下单与查看订单</p>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleSubmit">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" size="large" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              size="large"
              placeholder="请输入密码"
              show-password
              @keyup.enter="handleSubmit"
            />
          </el-form-item>
          <button class="mp-btn auth__submit" type="button" :disabled="loading" @click="handleSubmit">
            {{ loading ? '登录中…' : '登 录' }}
          </button>
        </el-form>

        <p class="auth__switch">
          还没有账号？<router-link to="/register" class="auth__link">立即注册</router-link>
        </p>
        <p class="auth__switch">
          <router-link to="/products" class="auth__link auth__link--muted">先随便逛逛</router-link>
        </p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useCartStore } from '@/store/cart'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(form)
    await cartStore.refresh()
    ElMessage.success('登录成功')
    // 从需要登录的页面跳转过来时，登录后回到原页面
    router.replace(route.query.redirect || '/products')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<!-- 版式样式在 styles/auth.css 中由登录/注册页共享，此处无需重复定义 -->
