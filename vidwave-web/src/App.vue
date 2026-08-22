<template>
  <div class="app">
    <!-- 如果没登录，显示登录页 -->
    <LoginPage v-if="!userStore.isLoggedIn()" />

    <!-- 如果已登录，显示完整的Web端布局 -->
    <div class="layout" v-else>
      <!-- 顶部导航栏（以后放Logo、搜索框、头像等） -->
      <header class="top-nav">
        <!-- 左侧 Logo -->
        <span class="logo">VidWave 微澜</span>

        <!-- 中间搜索框 -->
        <div class="search-box">
          <input type="text" placeholder="搜索你感兴趣的内容" />
          <span class="search-icon">🔍</span>
        </div>

        <!-- 右侧操作区 -->
        <div class="header-actions">
          <!-- 投稿按钮 -->
          <div class="action-item" @click="router.push('/upload')">
            <span class="action-icon">📤</span>
            <span class="action-text">投稿</span>
          </div>

          <!-- 消息按钮 + 弹窗 -->
          <div class="message-wrapper">
            <div class="action-item" @click="showMessage = !showMessage">
              <span class="action-icon">💬</span>
              <span class="action-text">消息</span>
            </div>

            <!-- 消息弹窗 -->
            <div class="message-panel" v-show="showMessage">
              <MessagePopup @close="showMessage = false" />
            </div>
          </div>

          <!-- 通知按钮 + 悬停弹窗 -->
          <div
            class="action-item notice-wrapper"
            @mouseenter="showNoticePopup"
            @mouseleave="hideNoticePopup"
          >
            <span class="action-icon">🔔</span>
            <span class="action-text">通知</span>

            <!-- 通知弹窗 -->
            <div class="notice-popup" v-show="showNotice">
              <div class="notice-item">💬 有 3 条新评论</div>
              <div class="notice-item">👍 你的视频被赞了</div>
              <div class="notice-item">👥 新粉丝关注了你</div>
            </div>
          </div>

          <!-- 头像 + 悬停弹窗 -->
          <div class="avatar-wrapper" @click="goProfile">
            <img
              :src="userStore.avatarUrl || '/default-avatar.svg/36'"
              class="avatar"
              alt="头像"
            />
            <!-- 悬停弹窗 -->
            <div class="popup">
              <div class="popup-item">我的喜欢</div>
              <div class="popup-item">我的收藏</div>
              <div class="popup-item">观看历史</div>
              <div class="popup-item">我的作品</div>
              <div class="popup-divider"></div>
              <div class="popup-item logout" @click.stop="handleLogout">
                退出登录
              </div>
            </div>
          </div>
        </div>
      </header>

      <div class="main-content">
        <!-- 左侧菜单栏（以后放精选、推荐、关注等） -->
        <aside class="side-menu">
          <!-- 第一部分：发现 -->
          <div class="menu-group">
            <router-link to="/featured" class="menu-item active">
              <span class="menu-icon">📺</span>
              <span>精选</span>
            </router-link>
            <router-link to="/recommend" class="menu-item">
              <span class="menu-icon">🔥</span>
              <span>推荐</span>
            </router-link>
            <router-link to="/AiVidwave" class="menu-item">
              <span class="menu-icon">🤖</span>
              <span>AI微澜</span>
            </router-link>
          </div>

          <div class="menu-divider"></div>

          <!-- 第二部分：社交 -->
          <div class="menu-group">
            <router-link to="/follow" class="menu-item">
              <span class="menu-icon">👥</span>
              <span>关注</span>
            </router-link>
            <router-link to="/friends" class="menu-item">
              <span class="menu-icon">👫</span>
              <span>朋友</span>
            </router-link>
            <router-link to="/profile" class="menu-item">
              <span class="menu-icon">👤</span>
              <span>我的</span>
            </router-link>
          </div>

          <div class="menu-divider"></div>

          <!-- 第三部分：更多 -->
          <div class="menu-group">
            <router-link to="/live" class="menu-item">
              <span class="menu-icon">📡</span>
              <span>直播</span>
            </router-link>
            <router-link to="/cinema" class="menu-item">
              <span class="menu-icon">🎬</span>
              <span>放映厅</span>
            </router-link>
            <router-link to="/drama" class="menu-item">
              <span class="menu-icon">🎭</span>
              <span>短剧</span>
            </router-link>
            <router-link to="/games" class="menu-item">
              <span class="menu-icon">🎮</span>
              <span>小游戏</span>
            </router-link>
          </div>
        </aside>

        <!-- 右侧内容区，页面组件在这里切换 -->
        <main class="content-area">
          <router-view />
        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import LoginPage from "./LoginPage.vue";
import MessagePopup from "./views/MessagePopup.vue";
import { useUserStore } from "./stores/userStore.js";

const router = useRouter();
const userStore = useUserStore();

// 通知弹窗状态
const showNotice = ref(false);
let noticeTimer = null;

// 消息弹窗状态
const showMessage = ref(false);

const handleLogout = () => {
  userStore.logout();
  router.push("/");
};

const goProfile = () => {
  router.push("/profile");
};

const showNoticePopup = () => {
  clearTimeout(noticeTimer);
  showNotice.value = true;
};

const hideNoticePopup = () => {
  noticeTimer = setTimeout(() => {
    showNotice.value = false;
  }, 300); // 延迟300毫秒消失
};
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.app {
  width: 100%;
  height: 100vh;
  background-color: black;
  color: white;
}

/* 顶部导航栏 */
.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 56px;
  border-bottom: 1px solid #333;
}

.logo {
  font-size: 20px;
  font-weight: bold;
}

.logout-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
  padding: 6px 14px;
  border-radius: 4px;
  cursor: pointer;
}
/* 搜索框 */
.search-box {
  display: flex;
  align-items: center;
  background: #222;
  border-radius: 20px;
  padding: 6px 16px;
  width: 300px;
}

.search-box input {
  background: transparent;
  border: none;
  color: white;
  outline: none;
  width: 100%;
  font-size: 13px;
}

.search-icon {
  color: #999;
  margin-left: 8px;
}

/* 右侧操作区 */
.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  color: #ccc;
}

.action-icon {
  font-size: 18px;
}

.action-text {
  font-size: 11px;
  margin-top: 2px;
}

/* 头像区域 */
.avatar-wrapper {
  position: relative;
  cursor: pointer;
  /* 关键：增大悬停热区，让鼠标在头像和弹窗之间移动时不会触发离开 */
  padding-bottom: 16px;
  margin-bottom: -16px;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

/* 悬停弹窗 */
.popup {
  position: absolute;
  top: 44px;
  right: 0;
  background: #222;
  border-radius: 8px;
  padding: 8px 0;
  min-width: 160px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.5);
  z-index: 200;

  /* 默认隐藏 */
  opacity: 0;
  visibility: hidden;
  /* 给弹窗上方留 12px 的隐形桥接区域 */
  padding-top: 12px;
  margin-top: -12px;

  /* 添加过渡动画 */
  transition: opacity 0.15s ease, visibility 0.15s ease;
}
/* 鼠标悬停时显示弹窗 */
.avatar-wrapper:hover .popup {
  opacity: 1;
  visibility: visible;
}

/* 弹窗内部内容区域（去掉顶部留白的影响） */
.popup-item:first-child {
  margin-top: 0;
}

.popup-item {
  padding: 10px 20px;
  color: #ccc;
  font-size: 14px;
  cursor: pointer;
  /* 确保文字区域不会被透明桥接区影响 */
  position: relative;
  z-index: 1;
}

.popup-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: white;
}

.popup-divider {
  height: 1px;
  background: #444;
  margin: 4px 12px;
  position: relative;
  z-index: 1;
}

.popup-item.logout {
  color: #ff6b6b;
}

/* 主体区域：左侧菜单 + 右侧内容 */
.main-content {
  display: flex;
  height: calc(100vh - 56px);
}

/* 左侧菜单栏 */
.side-menu {
  width: 200px;
  border-right: 1px solid #333;
  padding: 12px 0;
  overflow-y: auto;
}
/* 分割线 */
.menu-divider {
  height: 1px;
  background-color: #333;
  margin: 8px 12px;
}

/* 菜单项 */
.menu-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  color: #ccc;
  text-decoration: none;
  font-size: 14px;
  border-radius: 0;
  transition: background-color 0.15s ease;
}

.menu-item:hover {
  background-color: rgba(255, 255, 255, 0.08);
  color: white;
}

/* 激活状态的菜单项 */
.menu-item.router-link-active {
  color: white;
  font-weight: 500;
  background-color: rgba(255, 255, 255, 0.05);
}

/* 菜单图标 */
.menu-icon {
  width: 24px;
  margin-right: 10px;
  text-align: center;
  font-size: 16px;
}

/* 右侧内容区 */
.content-area {
  flex: 1;
  overflow: hidden;
}

/* 通知弹窗 */
.notice-wrapper {
  position: relative;
}

.notice-popup {
  position: absolute;
  top: 56px;
  right: 0;
  background: #222;
  border-radius: 8px;
  padding: 8px 0;
  min-width: 180px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.5);
  z-index: 200;
}

.notice-item {
  padding: 10px 16px;
  color: #ccc;
  font-size: 13px;
  cursor: pointer;
}

.notice-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: white;
}

/* 头像可点击 */
.avatar-wrapper {
  cursor: pointer;
}

/* 消息按钮容器 */
.message-wrapper {
  position: relative;
}

/* 消息面板定位在按钮正下方 */
.message-panel {
  position: absolute;
  top: 56px;
  right: 0;
  z-index: 300;
}
</style>