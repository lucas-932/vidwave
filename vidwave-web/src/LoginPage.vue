<template>
  <div class="login-container">
    <div class="login-card">
      <h1>微澜 VidWave</h1>
      <p class="subtitle">{{ isRegister ? "注册新账号" : "登录你的账号" }}</p>

      <!-- 用户名 -->
      <input
        v-model="username"
        type="text"
        placeholder="请输入用户名"
        class="input"
      />

      <!-- 密码 -->
      <input
        v-model="password"
        type="password"
        placeholder="请输入密码"
        class="input"
      />

      <!-- 验证码 -->
      <div class="captcha-row">
        <input
          v-model="captchaAnswer"
          type="number"
          placeholder="验证码答案"
          class="input captcha-input"
        />
        <span class="captcha-question" @click="refreshCaptcha">
          {{ captchaQuestion }}
        </span>
      </div>

      <!-- 登录/注册按钮 -->
      <button @click="handleSubmit" class="btn">
        {{ isRegister ? "注册" : "登录" }}
      </button>

      <!-- 切换登录/注册 -->
      <p class="switch-text">
        {{ isRegister ? "已有账号？" : "没有账号？" }}
        <span @click="isRegister = !isRegister" class="switch-link">
          {{ isRegister ? "去登录" : "去注册" }}
        </span>
      </p>

      <!-- 提示信息 -->
      <p v-if="message" class="message">{{ message }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import axios from "axios";
import { useUserStore } from "./stores/userStore.js";

const username = ref("");
const password = ref("");
const captchaKey = ref("");
const captchaQuestion = ref("点击获取验证码");
const captchaAnswer = ref("");
const isRegister = ref(false);
const message = ref("");

const userStore = useUserStore();

// 获取验证码
const refreshCaptcha = async () => {
  try {
    const res = await axios.get("/api/captcha/generate");
    if (res.data.code === 200) {
      captchaKey.value = res.data.captchaKey;
      captchaQuestion.value = res.data.question;
    }
  } catch (error) {
    message.value = "验证码获取失败，请重试";
    console.error(error);
  }
};

// 页面加载时自动获取一次
onMounted(() => {
  refreshCaptcha();
});

const handleSubmit = async () => {
  if (!username.value || !password.value) {
    message.value = "用户名和密码不能为空";
    return;
  }
  if (!captchaAnswer.value) {
    message.value = "请输入验证码答案";
    return;
  }

  const url = isRegister.value ? "/api/user/register" : "/api/user/login";

  try {
    const response = await axios.post(url, {
      username: username.value,
      password: password.value,
      captchaKey: captchaKey.value,
      captchaAnswer: parseInt(captchaAnswer.value),
    });

    if (response.data.code === 200) {
      if (isRegister.value) {
        message.value = "注册成功，请登录";
        isRegister.value = false;
        refreshCaptcha();
      } else {
        userStore.setLoginInfo(
          response.data.token,
          username.value,
          response.data.avatarUrl,
          response.data.userId
        );
      }
    } else {
      message.value = response.data.message;
      refreshCaptcha(); // 验证码错误后刷新
    }
  } catch (error) {
    message.value = "网络错误，请重试";
    console.error(error);
  }
};
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

/* 验证码行 */
.captcha-row {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.captcha-input {
  flex: 1;
  margin-bottom: 0;
}

.captcha-question {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 110px;
  background: #f0f0f0;
  border-radius: 6px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  user-select: none;
}

.captcha-question:hover {
  background: #e0e0e0;
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