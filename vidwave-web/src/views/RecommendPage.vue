<template>
  <div class="recommend-page">
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
            <div class="action-btn" @click="toggleLike(video)">
              <span :style="{ color: video.isLiked ? '#ff4d4f' : 'white' }"
                >❤️</span
              >
              {{ video.likeCount }}
            </div>
            <div class="action-btn">💬 {{ video.commentCount }}</div>
            <div class="action-btn">🔗 0</div>
          </div>
          <div class="title">{{ video.title }}</div>
        </div>
      </swiper-slide>
    </swiper>

    <div v-if="videos.length === 0" class="loading">微澜正在赶来...</div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from "vue";
import { Swiper, SwiperSlide } from "swiper/vue";
import { Navigation, Pagination, Mousewheel } from "swiper/modules";
import "swiper/css";
import { getVideos } from "../api/index.js";
import { useUserStore } from "../stores/userStore.js";
import axios from "axios";

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

// 获取点赞状态（在获取视频列表后调用）
const fetchLikeStatus = async () => {
  for (const video of videos.value) {
    try {
      const res = await axios.get("http://localhost:8080/api/like/status", {
        params: { userId: userStore.userId, videoId: video.id },
      });
      video.isLiked = res.data.isLiked;
      video.likeCount = res.data.likeCount;
    } catch (e) {
      console.error("获取点赞状态失败", e);
    }
  }
};

// 点赞/取消点赞
const toggleLike = async (video) => {
  try {
    const res = await axios.post("http://localhost:8080/api/like/toggle", {
      userId: userStore.userId,
      videoId: video.id,
    });
    if (res.data.code === 200) {
      video.isLiked = res.data.isLiked;
      // 根据结果加减
      if (res.data.isLiked) {
        video.likeCount++;
      } else {
        video.likeCount--;
      }
    }
  } catch (e) {
    console.error("点赞失败", e);
  }
};

onMounted(async () => {
  try {
    const response = await getVideos(1, 10);
    videos.value = response.data.data.records;

    await fetchLikeStatus(); // 新增

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

.recommend-page {
  width: 100%;
  height: 100vh;
  background-color: black;
  overflow: hidden;
  position: relative;
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
.video-swiper .swiper-slide-active video {
  opacity: 1;
}
</style>