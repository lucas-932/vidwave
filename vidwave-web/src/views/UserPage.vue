<template>
  <div class="user-page">
    <!-- 返回按钮 -->
    <div class="back-btn" @click="goBack">← 返回</div>

    <!-- 用户信息头部 -->
    <div class="user-header">
      <img
        :src="userInfo.avatarUrl || '/default-avatar.svg'"
        class="user-avatar"
        alt="头像"
      />
      <h2 class="user-name">{{ userInfo.username || "未知用户" }}</h2>
      <div class="user-stats">
        <div class="stat-item">
          <span class="stat-num">{{ videos.length }}</span>
          <span class="stat-label">作品</span>
        </div>
      </div>
    </div>

    <!-- 作品网格 -->
    <div class="works-grid">
      <div
        v-for="video in videos"
        :key="video.id"
        class="work-item"
        @click="openVideo(video)"
      >
        <img
          :src="video.coverUrl || '/video-placeholder.svg'"
          class="work-cover"
          alt="封面"
        />
        <div class="work-info">
          <span class="work-title">{{ video.title }}</span>
        </div>
      </div>

      <div v-if="videos.length === 0" class="empty-tip">暂无作品</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import axios from "axios";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();
const userId = route.params.id;

const userInfo = ref({});
const videos = ref([]);

const goBack = () => {
  router.back();
};

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const res = await axios.get(`/api/user/${userId}`);
    if (res.data.code === 200) {
      userInfo.value = res.data.data;
    }
  } catch (e) {
    console.error("获取用户信息失败", e);
  }
};

// 获取该用户的作品
const fetchUserVideos = async () => {
  try {
    const res = await axios.get("/api/videos/my", {
      params: { userId: userId },
    });
    if (res.data.code === 200) {
      videos.value = res.data.data;
    }
  } catch (e) {
    console.error("获取用户作品失败", e);
  }
};

const openVideo = (video) => {
  router.push({
    path: "/works",
    query: {
      authorId: userId,
      videoId: video.id,
    },
  });
};

onMounted(() => {
  fetchUserInfo();
  fetchUserVideos();
});
</script>

<style scoped>
.back-btn {
  position: fixed;
  top: 76px; /* 避开顶部导航栏 */
  left: 220px; /* 避开左侧菜单 */
  z-index: 100;
  color: white;
  background: rgba(0, 0, 0, 0.5);
  padding: 8px 14px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
}

.back-btn:hover {
  background: rgba(0, 0, 0, 0.7);
}

.user-page {
  padding: 40px;
  background-color: #111;
  color: white;
  height: 100%;
  overflow-y: auto;
}

.user-header {
  text-align: center;
  margin-bottom: 30px;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.3);
  object-fit: cover;
  margin-bottom: 12px;
}

.user-name {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 16px;
}

.user-stats {
  display: flex;
  justify-content: center;
  gap: 40px;
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

.works-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
}

.work-item {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  background: #1e1e1e;
  transition: transform 0.2s;
}

.work-item:hover {
  transform: scale(1.02);
}

.work-cover {
  width: 100%;
  height: 180px;
  object-fit: cover;
  display: block;
}

.work-info {
  padding: 8px 10px;
}

.work-title {
  color: white;
  font-size: 13px;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-tip {
  text-align: center;
  color: #666;
  padding: 40px 0;
  grid-column: 1 / -1;
}
</style>