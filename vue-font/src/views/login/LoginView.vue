<template>
  <div class="login-container">
    <div class="decoration-1"></div>
    <div class="decoration-2"></div>
    <div class="decoration-3"></div>
    <div class="decoration-4"></div>
    <div class="login-content">
      <div class="login-header">
        <div class="logo">
          <el-icon :size="38">🔑</el-icon>
        </div>
        <h1 class="title">插件工厂管理平台</h1>
        <p class="subtitle">欢迎回来，请登录您的账号</p>
      </div>

      <el-card class="login-card" :body-style="{ padding: '30px' }" shadow="hover">
        <el-form :model="form" :rules="rules" ref="formRef" label-position="top" status-icon>
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              prefix-icon="User"
              autocomplete="off"
              size="large"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              type="password"
              v-model="form.password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              autocomplete="off"
              size="large"
              show-password
            />
          </el-form-item>

          <div class="remember-forgot">
            <el-checkbox v-model="form.remember">记住我</el-checkbox>
          </div>

          <el-form-item>
            <el-button
              @click="handleLogin"
              :loading="loading"
              class="login-button"
              size="large"
              round
            >
              登录系统
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <div class="login-footer">
        <p>© {{ new Date().getFullYear() }} 管理系统 - 版权所有</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  username: '',
  password: '',
  remember: false,
})

// 初始化时加载记住的用户名
onMounted(() => {
  if (userStore.rememberedUsername) {
    form.value.username = userStore.rememberedUsername
    form.value.remember = true
  }
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' },
  ],
}

const formRef = ref()
const loading = ref(false)

const handleLogin = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.login(form.value)
      ElMessage.success('登录成功')
      router.push('/')
    } catch (e) {
      console.error('login failed', e)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.15) 0%, rgba(6, 182, 212, 0.15) 100%);
  top: -100px;
  right: -100px;
  filter: blur(20px);
  animation: float 8s ease-in-out infinite;
}

.login-container::after {
  content: '';
  position: absolute;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(6, 182, 212, 0.1) 0%, rgba(139, 92, 246, 0.1) 100%);
  bottom: -50px;
  left: -50px;
  filter: blur(15px);
}

.login-container .geometric-pattern {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  opacity: 0.05;
  pointer-events: none;
  background-image:
    radial-gradient(circle at 20% 30%, rgba(139, 92, 246, 0.8) 0%, transparent 25%),
    radial-gradient(circle at 80% 70%, rgba(6, 182, 212, 0.8) 0%, transparent 20%),
    linear-gradient(
      45deg,
      transparent 48%,
      rgba(139, 92, 246, 0.3) 49%,
      rgba(139, 92, 246, 0.3) 51%,
      transparent 52%
    ),
    linear-gradient(
      -45deg,
      transparent 48%,
      rgba(6, 182, 212, 0.3) 49%,
      rgba(6, 182, 212, 0.3) 51%,
      transparent 52%
    );
  background-size:
    300px 300px,
    250px 250px,
    100px 100px,
    100px 100px;
  background-position:
    0 0,
    100px 100px,
    50px 50px,
    150px 150px;
  background-repeat: repeat;
  animation: patternMove 30s linear infinite;
}

@keyframes patternMove {
  0% {
    background-position:
      0 0,
      100px 100px,
      50px 50px,
      150px 150px;
  }
  100% {
    background-position:
      100px 100px,
      200px 200px,
      150px 150px,
      250px 250px;
  }
}
.login-container .decoration-1 {
  position: absolute;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.08) 0%, rgba(6, 182, 212, 0.08) 100%);
  top: 20%;
  left: 10%;
  filter: blur(10px);
  animation: float 12s ease-in-out infinite;
}

.login-container .decoration-2 {
  position: absolute;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(6, 182, 212, 0.08) 0%, rgba(139, 92, 246, 0.08) 100%);
  bottom: 20%;
  right: 15%;
  filter: blur(8px);
  animation: float 15s ease-in-out infinite reverse;
}

.login-container .decoration-3 {
  position: absolute;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.06) 0%, rgba(6, 182, 212, 0.06) 100%);
  top: 60%;
  left: 20%;
  filter: blur(5px);
  animation: float 18s ease-in-out infinite;
}

.login-container .decoration-4 {
  position: absolute;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(6, 182, 212, 0.06) 0%, rgba(139, 92, 246, 0.06) 100%);
  top: 30%;
  right: 25%;
  filter: blur(6px);
  animation: float 20s ease-in-out infinite reverse;
}

@keyframes float {
  0%,
  100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(20px, -20px) scale(1.05);
  }
  50% {
    transform: translate(-10px, 10px) scale(0.95);
  }
  75% {
    transform: translate(15px, 15px) scale(1.02);
  }
}

.login-content {
  width: 420px;
  position: relative;
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;

  .logo {
    display: inline-flex;
    justify-content: center;
    align-items: center;
    width: 64px;
    height: 64px;
    border-radius: 16px;
    background: linear-gradient(135deg, #8b5cf6 0%, #06b6d4 100%);
    color: white;
    margin-bottom: 16px;
    box-shadow: 0 4px 16px rgba(139, 92, 246, 0.3);
    transition: all 0.3s ease;
  }

  .logo:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(139, 92, 246, 0.4);
  }

  .title {
    font-size: 28px;
    font-weight: 600;
    color: #1e293b;
    margin: 0 0 8px;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .subtitle {
    font-size: 16px;
    color: #64748b;
    margin: 0;
  }
}

.login-card {
  border-radius: 20px;
  box-shadow:
    0 10px 40px rgba(139, 92, 246, 0.12),
    0 0 0 1px rgba(255, 255, 255, 0.5);
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;

  :deep(.el-card__body) {
    padding: 30px;
    position: relative;
    z-index: 1;
  }
}

.login-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #8b5cf6 0%, #06b6d4 100%);
  opacity: 0.7;
}

.login-card:hover {
  box-shadow:
    0 15px 50px rgba(139, 92, 246, 0.18),
    0 0 0 1px rgba(139, 92, 246, 0.2);
  transform: translateY(-3px);
  border: 1px solid rgba(139, 92, 246, 0.3);
  background: rgba(255, 255, 255, 0.9);
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;

  .el-icon {
    font-size: 16px;
    color: #9ca3af;
  }
}

.remember-forgot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.login-button {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  margin-top: 10px;
  background: linear-gradient(135deg, #8b5cf6 0%, #06b6d4 100%);
  border: none;
  color: white;
  font-weight: 500;
  box-shadow: 0 4px 16px rgba(139, 92, 246, 0.3);
  transition: all 0.3s ease;
}

.login-button:hover {
  background: linear-gradient(135deg, #7c3aed 0%, #0891b2 100%);
  box-shadow: 0 6px 20px rgba(139, 92, 246, 0.4);
  transform: translateY(-1px);
}

.login-footer {
  text-align: center;
  color: #64748b;
  font-size: 14px;
  margin-top: 16px;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px rgba(139, 92, 246, 0.2) inset;
  border-radius: 8px;
  padding: 4px 12px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(5px);
  color: #1e293b;
  transition: all 0.3s ease;

  &.is-focus {
    box-shadow: 0 0 0 2px #8b5cf6 inset;
    background: rgba(255, 255, 255, 0.9);
  }
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #8b5cf6 inset;
}

:deep(.el-input__inner) {
  color: #1e293b;
  background: transparent;
}

:deep(.el-button) {
  font-weight: 500;
  background: linear-gradient(135deg, #8b5cf6 0%, #06b6d4 100%);
  border: none;
  color: white;
  transition: all 0.3s ease;
}

:deep(.el-button:hover) {
  background: linear-gradient(135deg, #7c3aed 0%, #0891b2 100%);
  transform: translateY(-1px);
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #1e293b;
}

:deep(.el-checkbox__label) {
  color: #1e293b;
}
</style>
