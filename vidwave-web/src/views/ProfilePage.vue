<template>
  <div class="profile-page">
    <!-- 头像 + 昵称 -->
    <div class="profile-header">
      <img
        :src="userStore.avatarUrl || '/default-avatar.svg/80'"
        class="profile-avatar"
        alt="头像"
      />
      <h2 class="profile-username">{{ userStore.username }}</h2>
    </div>

    <!-- 数据统计 -->
    <div class="profile-stats">
      <div class="stat-item">
        <span class="stat-num">0</span>
        <span class="stat-label">获赞</span>
      </div>
      <div class="stat-item">
        <span class="stat-num">0</span>
        <span class="stat-label">关注</span>
      </div>
      <div class="stat-item">
        <span class="stat-num">0</span>
        <span class="stat-label">粉丝</span>
      </div>
    </div>

    <!-- 标签页 -->
    <div class="profile-tabs">
      <span
        :class="{ active: currentTab === 'works' }"
        @click="currentTab = 'works'"
        >作品</span
      >
      <span
        :class="{ active: currentTab === 'likes' }"
        @click="currentTab = 'likes'"
        >喜欢</span
      >
      <span
        :class="{ active: currentTab === 'collects' }"
        @click="currentTab = 'collects'"
        >收藏</span
      >
    </div>

    <!-- 视频列表占位 -->
    <div class="tab-content">
      <p v-if="currentTab === 'works'">暂无作品</p>
      <p v-if="currentTab === 'likes'">暂无喜欢的视频</p>
      <p v-if="currentTab === 'collects'">暂无收藏</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useUserStore } from "../stores/userStore.js";

const userStore = useUserStore();
const currentTab = ref("works");
</script>

<style scoped>
.profile-page {
  padding: 40px;
  background-color: #111;
  color: white;
  height: 100%;
  overflow-y: auto;
}

.profile-header {
  text-align: center;
  margin-bottom: 24px;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.3);
  object-fit: cover;
  margin-bottom: 12px;
}

.profile-username {
  font-size: 20px;
  font-weight: 600;
}

.profile-stats {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-bottom: 32px;
}

.stat-item {
  text-align: center;
}

.stat-num {
  font-size: 18px;
  font-weight: bold;
}

.stat-label {
  font-size: 12px;
  color: #999;
  display: block;
  margin-top: 4px;
}

.profile-tabs {
  display: flex;
  justify-content: center;
  gap: 32px;
  border-bottom: 1px solid #333;
  padding-bottom: 12px;
  margin-bottom: 24px;
}

.profile-tabs span {
  cursor: pointer;
  color: #999;
  font-size: 14px;
  padding-bottom: 8px;
}

.profile-tabs span.active {
  color: white;
  font-weight: 600;
  border-bottom: 2px solid white;
}

.tab-content {
  text-align: center;
  color: #666;
  padding: 40px 0;
}
</style>