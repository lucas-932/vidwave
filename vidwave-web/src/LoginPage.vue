<template>
  <div class="login-container">
    <div class="login-card">
      <h1>微澜 VidWave</h1>
      <p class="subtitle">{{ isRegister ? '注册新账号' : '登录你的账号' }}</p>

      <!-- 用户名输入框 -->
      <input
        v-model="username"
        type="text"
        placeholder="请输入用户名"
        class="input"
      />

      <!-- 密码输入框 -->
      <input
        v-model="password"
        type="password"
        placeholder="请输入密码"
        class="input"
      />

      <!-- 登录/注册按钮 -->
      <button @click="handleSubmit" class="btn">
        {{ isRegister ? '注册' : '登录' }}
      </button>

      <!-- 切换登录/注册 -->
      <p class="switch-text">
        {{ isRegister ? '已有账号？' : '没有账号？' }}
        <span @click="isRegister = !isRegister" class="switch-link">
          {{ isRegister ? '去登录' : '去注册' }}
        </span>
      </p>

      <!-- 提示信息 -->
      <p v-if="message" class="message">{{ message }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useUserStore } from './stores/userStore.js'

const username = ref('')
const password = ref('')
const isRegister = ref(false)  // true=注册模式, false=登录模式
const message = ref('')

const userStore = useUserStore()

const handleSubmit = async () => {
  // 简单校验
  if (!username.value || !password.value) {
    message.value = '用户名和密码不能为空'
    return
  }

  // 根据模式选择接口地址
  const url = isRegister.value
    ? 'http://localhost:8080/api/user/register'
    : 'http://localhost:8080/api/user/login'

  try {
    const response = await axios.post(url, {
      username: username.value,
      password: password.value
    })

    if (response.data.code === 200) {
      if (isRegister.value) {
        // 注册成功，切换到登录模式
        message.value = '注册成功，请登录'
        isRegister.value = false
      } else {
        // 登录成功，保存 Token 和用户名
        userStore.setLoginInfo(response.data.token, username.value)
        message.value = ''
        // 不需要手动跳转，App.vue 会自动切换页面
      }
    } else {
      message.value = response.data.message
    }
  } catch (error) {
    message.value = '网络错误，请重试'
    console.error(error)
  }
}
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  background: white;
  padding: 40px 30px;
  border-radius: 12px;
  width: 320px;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

h1 {
  margin: 0 0 5px;
  color: #333;
}

.subtitle {
  color: #888;
  font-size: 14px;
  margin-bottom: 25px;
}

.input {
  width: 100%;
  padding: 12px;
  margin-bottom: 15px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
}

.btn {
  width: 100%;
  padding: 12px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  margin-top: 5px;
}

.btn:hover {
  background: #5a6fd6;
}

.switch-text {
  margin-top: 20px;
  font-size: 14px;
  color: #888;
}

.switch-link {
  color: #667eea;
  cursor: pointer;
  font-weight: bold;
}

.switch-link:hover {
  text-decoration: underline;
}

.message {
  margin-top: 15px;
  color: #e74c3c;
  font-size: 14px;
}
</style>