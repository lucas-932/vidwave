<template>
  <div class="works-player">
    <swiper
      :direction="'vertical'"
      :slides-per-view="1"
      :space-between="0"
      :modules="modules"
      :mousewheel="true"
      :initial-slide="initialIndex"
      @swiper="onSwiper"
      @slide-change="onSlideChange"
      class="video-swiper"
    >
      <swiper-slide v-for="video in videos" :key="video.id">
        <div class="video-container">
          <video
            :ref="(el) => setVideoRef(el, video.id)"
            :src="video.videoUrl"
            :poster="video.coverUrl || '/video-placeholder.svg'"
            muted
            loop
            playsinline
            webkit-playsinline
            preload="metadata"
            @loadedmetadata="handleLoaded(video.id)"
          />
          <div class="video-info">
            <div class="title">{{ video.title }}</div>
            <div v-if="video.tags" class="tags">
              <span v-for="tag in video.tags.split(',')" :key="tag" class="tag"
                >#{{ tag }}</span
              >
            </div>
          </div>
        </div>
      </swiper-slide>
    </swiper>

    <!-- 返回按钮 -->
    <div class="back-btn" @click="goBack">← 返回</div>

    <div v-if="videos.length === 0" class="loading">暂无作品</div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Swiper, SwiperSlide } from "swiper/vue";
import { Navigation, Pagination, Mousewheel } from "swiper/modules";
import "swiper/css";
import axios from "axios";

const route = useRoute();
const router = useRouter();

const modules = [Navigation, Pagination, Mousewheel];
const videos = ref([]);
const authorId = route.query.authorId;
const targetVideoId = Number(route.query.videoId);

const videoRefs = {};
const setVideoRef = (el, id) => {
  if (el) videoRefs[id] = el;
};

let swiperInstance = null;
let currentIndex = 0;

const initialIndex = computed(() => {
  const idx = videos.value.findIndex((v) => v.id === targetVideoId);
  return idx >= 0 ? idx : 0;
});

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

const goBack = () => {
  router.back();
};

onMounted(async () => {
  try {
    const res = await axios.get("/api/videos/my", {
      params: { userId: authorId },
    });
    if (res.data.code === 200) {
      videos.value = res.data.data;
      // 等 DOM 更新后让 Swiper 跳到初始视频
      nextTick(() => {
        if (swiperInstance && videos.value.length > 0) {
          const startIndex = initialIndex.value;
          if (startIndex > 0) {
            swiperInstance.slideTo(startIndex, 0);
          } else {
            playCurrentVideo();
          }
        }
      });
    }
  } catch (e) {
    console.error("获取作品失败", e);
  }
});
</script>

<style scoped>
.works-player {
  width: 100%;
  height: 100vh;
  background-color: black;
  position: relative;
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

.video-info {
  position: absolute;
  bottom: 40px;
  left: 16px;
  z-index: 10;
  max-width: 70%;
}

.title {
  color: white;
  font-size: 16px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.7);
  margin-bottom: 6px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  color: rgba(255, 255, 255, 0.75);
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 12px;
}

.back-btn {
  position: absolute;
  top: 20px;
  left: 16px;
  z-index: 100;
  color: white;
  background: rgba(0, 0, 0, 0.5);
  padding: 8px 14px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
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