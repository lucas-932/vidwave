<template>
  <div class="app">
    <!-- 如果没登录，显示登录页 -->
    <LoginPage v-if="!userStore.isLoggedIn()" />

    <!-- 如果已登录，显示视频滑动页 -->
    <template v-else>
      <!-- 顶部栏：头像 + 用户名 + 退出 -->
      <div class="top-bar">
        <img
          :src="userStore.avatarUrl || 'https://via.placeholder.com/40'"
          class="avatar"
          alt="头像"
        />
        <span class="username">{{ userStore.username }}</span>
        <button @click="handleLogout" class="logout-btn">退出</button>
      </div>

      <!-- 视频滑动区 -->
      <swiper
        :direction="'vertical'"
        :slides-per-view="1"
        :space-between="0"
        :modules="modules"
        :mousewheel="true"
        @swiper="onSwiper"
        @slide-change="onSlideChange"
        class="video-swiper"
      >
        <swiper-slide v-for="video in videos" :key="video.id">
          <div class="video-container">
            <video
              :ref="(el) => setVideoRef(el, video.id)"
              :src="video.videoUrl"
              :poster="video.coverUrl"
              muted
              loop
              playsinline
              webkit-playsinline
              preload="metadata"
              @loadedmetadata="handleLoaded(video.id)"
            />

            <div class="actions">
              <div class="action-btn">❤️ {{ video.likeCount }}</div>
              <div class="action-btn">💬 {{ video.commentCount }}</div>
              <div class="action-btn">🔗 0</div>
            </div>
            <div class="title">{{ video.title }}</div>
          </div>
        </swiper-slide>
      </swiper>

      <div v-if="videos.length === 0" class="loading">微澜正在赶来...</div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from "vue";
import { Swiper, SwiperSlide } from "swiper/vue";
import { Navigation, Pagination, Mousewheel } from "swiper/modules";
import "swiper/css";
import { getVideos } from "./api/index.js";
import LoginPage from "./LoginPage.vue";
import { useUserStore } from "./stores/userStore.js";

const modules = [Navigation, Pagination, Mousewheel];
const videos = ref([]);
const userStore = useUserStore();

const videoRefs = {};
const setVideoRef = (el, id) => {
  if (el) videoRefs[id] = el;
};

let swiperInstance = null;
let currentIndex = 0;

const onSwiper = (swiper) => {
  swiperInstance = swiper;
  nextTick(() => playCurrentVideo());
};

const onSlideChange = (swiper) => {
  Object.values(videoRefs).forEach((v) => v.pause());
  currentIndex = swiper.activeIndex;
  playCurrentVideo();
};

const playCurrentVideo = () => {
  const videoList = videos.value;
  if (videoList.length === 0) return;
  const currentVideo = videoList[currentIndex];
  const videoEl = videoRefs[currentVideo.id];
  if (videoEl) {
    videoEl.currentTime = 0;
    videoEl.play().catch((err) => console.warn("自动播放失败：", err));
  }
};

const handleLoaded = (id) => {
  if (videos.value[currentIndex]?.id === id) {
    playCurrentVideo();
  }
};

const handleLogout = () => {
  userStore.logout();
};

onMounted(async () => {
  try {
    const response = await getVideos(1, 10);
    videos.value = response.data.data.records;
    nextTick(() => {
      if (swiperInstance && videos.value.length > 0) {
        playCurrentVideo();
      }
    });
  } catch (error) {
    console.error("请求视频列表失败：", error);
  }
});
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
  overflow: hidden;
  position: relative;
}

/* 顶部栏 */
.top-bar {
  position: absolute;
  top: 20px;
  left: 16px;
  right: 16px;
  display: flex;
  align-items: center;
  z-index: 100;
  color: white;
  /* 防止顶部栏被 Swiper 盖住 */
  pointer-events: none;
}
.top-bar > * {
  pointer-events: auto;
}

.avatar {
   width: 36px;
  height: 36px;
  border-radius: 50%;
  margin-right: 12px;
  border: 1.5px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 2px 8px rgba(0,0,0,0.3);
  object-fit: cover;
}

.username {
  font-size: 16px;
  font-weight: 500;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.7);
}

.logout-btn {
   margin-left: auto;
  background: rgba(0, 0, 0, 0.3);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.4);
  padding: 5px 14px;
  border-radius: 20px; /* 圆角胶囊按钮 */
  cursor: pointer;
  font-size: 13px;
  backdrop-filter: blur(10px); /* 毛玻璃效果 */
  transition: all 0.2s;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* Swiper */
.video-swiper {
  width: 100%;
  height: 100%;
}

.video-container {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.video-container video {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background-color: black;
}

/* 右侧互动按钮 */
.actions {
  position: absolute;
  right: 16px;
  bottom: 120px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  z-index: 10;
}

.action-btn {
  color: white;
  font-size: 18px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
  cursor: pointer;
  user-select: none;
}

/* 底部标题 */
.title {
  position: absolute;
  bottom: 40px;
  left: 16px;
  color: white;
  font-size: 16px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.7);
  max-width: 70%;
  z-index: 10;
}

/* 加载提示 */
.loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-size: 20px;
}

/* Swiper 滑动时的过渡动画 */
.video-swiper .swiper-slide {
  transition: opacity 0.3s ease;
}
.video-swiper .swiper-slide video {
  transition: opacity 0.5s ease;
}
/* 确保即将进入的slide视频透明度正常 */
.video-swiper .swiper-slide-active video {
  opacity: 1;
}
</style>