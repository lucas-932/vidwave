<template>
  <div class="app">
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
          </div>
          <div class="title">{{ video.title }}</div>
        </div>
      </swiper-slide>
    </swiper>
    <div v-if="videos.length === 0" class="loading">加载中...</div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from "vue";
import { Swiper, SwiperSlide } from "swiper/vue";
import { Navigation, Pagination, Mousewheel } from "swiper/modules";
import "swiper/css";
import { getVideos } from "./api/index.js";

const modules = [Navigation, Pagination, Mousewheel];
const videos = ref([]);

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
  // 暂停所有视频
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
}

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

.loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-size: 20px;
}
</style>