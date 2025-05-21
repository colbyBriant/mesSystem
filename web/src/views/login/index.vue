<template>
  <div class="login-container">
    <!-- 背景图层 -->
    <div class="bg-wrapper">
      <img :src="backgroundImage" class="bg-image" alt="背景" />
      <div class="bg-gradient"></div>
    </div>

    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="logo">
        <img :src="logoImage" alt="Logo" class="logo-image" />
        <span class="system-title">中医体质辨识系统 v1.0.0</span>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧标题 -->
      <div class="left-content">
        <h1 class="main-title">中医体质辨识</h1>
        <h2 class="main-title">健康管理系统</h2>
        <p class="description">
          基于传统中医理论，结合现代科技，为您提供专业的体质辨识和健康管理服务。
        </p>
      </div>

      <!-- 登录框 -->
      <div class="login-box">
        <h2 class="login-title">
          {{ isRegister ? '用户注册' : '欢迎登录' }}
        </h2>
        
        <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              class="custom-input"
              autocomplete="off"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              class="custom-input"
              show-password
              autocomplete="new-password"
              @keyup.enter="handleSubmit"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <!-- 只在注册模式下显示角色选择 -->
          <el-form-item v-if="isRegister" prop="role">
            <el-select 
              v-model="form.role" 
              placeholder="请选择角色" 
              class="custom-select"
            >
              <el-option label="管理员" value="ADMIN" />
              <el-option label="医生" value="DOCTOR" />
              <el-option label="患者" value="PATIENT" />
            </el-select>
          </el-form-item>

          <!-- 移除department字段，因为后端登录接口不需要 -->

          <el-button
            :loading="loading"
            type="primary"
            class="submit-button"
            @click="handleSubmit"
          >
            {{ isRegister ? '注册' : '登录' }}
          </el-button>

          <div class="toggle-mode">
            <el-link type="primary" :underline="false" @click="toggleMode">
              {{ isRegister ? '已有账号？去登录' : '没有账号？去注册' }}
            </el-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

// 图片资源导入
const backgroundImage = new URL('@/assets/images/background.jpg', import.meta.url).href
const logoImage = new URL('@/assets/images/logo.png', import.meta.url).href

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const isRegister = ref(false)

// 简化表单，只保留后端需要的字段
const form = reactive({
  username: '',
  password: '',
  role: ''
})

// 相应调整验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符之间', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    if (isRegister.value) {
      await userStore.register(form)
      ElMessage.success('注册成功')
      isRegister.value = false
    } else {
      await userStore.login(form)
      ElMessage.success('登录成功')
      router.push('/index')
    }
  } catch (error) {
    console.error('详细错误信息:', error)
    const errorMessage = error.response?.data?.message 
      || error.message 
      || `${isRegister.value ? '注册' : '登录'}失败`
    ElMessage.error(errorMessage)
  } finally {
    loading.value = false
  }
}

const toggleMode = () => {
  isRegister.value = !isRegister.value
  form.role = ''
  formRef.value?.resetFields()
}

</script>
<style scoped>
.login-container {
  min-height: 100vh;
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.bg-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.bg-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.8;
}

.bg-gradient {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: none;
}

.header {
  position: relative;
  z-index: 10;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 32px;
}

.logo {
  display: flex;
  align-items: center;
}

.logo-image {
  height: 32px;
  margin-right: 12px;
}

.system-title {
  color: white;
  font-size: 20px;
  font-weight: 500;
}

.window-controls {
  display: flex;
  gap: 8px;
}

.main-content {
  position: relative;
  z-index: 10;
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 120px;
  margin-top: 60px;
}

.left-content {
  color: white;
  max-width: 600px;
}

.main-title {
  font-family: "STXingkai", "华文行楷", serif;
  font-size: 48px;
  margin-bottom: 20px;
  text-shadow: 2px 2px 8px rgba(0, 0, 0, 0.6);
  letter-spacing: 4px;
  color: #ffffff;
  font-weight: bold;
}

.description {
  font-size: 18px;
  line-height: 1.6;
  opacity: 1;
  margin-top: 30px;
  text-shadow: 1px 1px 4px rgba(0, 0, 0, 0.5);
  padding: 15px;
  border-radius: 8px;
}

.login-box {
  background: rgba(255, 255, 255, 0.95);
  padding: 32px;
  border-radius: 8px;
  width: 400px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.login-title {
  font-size: 24px;
  font-weight: 500;
  text-align: center;
  margin-bottom: 32px;
  color: #333;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.custom-input {
  width: 100%;
}

.custom-select {
  width: 100%;
}

.submit-button {
  width: 100%;
  height: 40px;
  font-size: 16px;
  background: linear-gradient(to right, #1890ff, #096dd9);
  border: none;
  margin-top: 8px;
}

.submit-button:hover {
  background: linear-gradient(to right, #096dd9, #0050b3);
  transform: translateY(-1px);
}

.toggle-mode {
  text-align: center;
  margin-top: 16px;
}

.control-btn {
  background: rgba(255, 255, 255, 0.2) !important;
  border: none !important;
  backdrop-filter: blur(4px);
}

.control-btn:hover {
  background: rgba(255, 255, 255, 0.3) !important;
}

:deep(.el-input__wrapper) {
  background: white !important;
  border: 1px solid #dcdfe6;
  box-shadow: none !important;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #409eff;
}

:deep(.el-form-item__error) {
  margin-top: 4px;
}
</style>