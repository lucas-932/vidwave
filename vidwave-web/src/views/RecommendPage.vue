<template>
  <div class="recommend-page">
    <!-- 视频区域 -->
    <div class="video-area" :class="{ shrink: showComments }">
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

            <!-- 右侧操作栏 -->
            <div class="actions">
              <!-- 作者头像 + 关注按钮 -->
              <div class="author-area">
                <img
                  :src="video.author?.avatar || '/default-avatar.svg'"
                  class="author-avatar"
                  alt="作者头像"
                />
                <div
                  class="follow-btn"
                  :class="{ followed: video.isFollowed }"
                  @click.stop="toggleFollow(video)"
                >
                  {{ video.isFollowed ? "✓" : "+" }}
                </div>
              </div>

              <!-- 点赞 -->
              <div class="action-btn" @click="toggleLike(video)">
                <span v-if="video.isLiked" style="color: #ff4d4f">❤️</span>
                <span v-else style="color: white">🤍</span>
                <span class="action-num">{{ video.likeCount }}</span>
              </div>

              <!-- 评论 -->
              <div class="action-btn" @click="openComments(video)">
                <span>💬</span>
                <span class="action-num">{{ video.commentCount }}</span>
              </div>

              <!-- 分享 -->
              <div class="action-btn">
                <span>🔗</span>
                <span class="action-num">分享</span>
              </div>
            </div>
            <div class="title">{{ video.title }}</div>
          </div>
        </swiper-slide>
      </swiper>

      <div v-if="videos.length === 0" class="loading">微澜正在赶来...</div>
    </div>
    <!-- 评论区（右侧滑出面板） -->
    <div class="comment-section" v-if="showComments">
      <CommentPanel
        :videoId="activeVideoId"
        @close="showComments = false"
        @comment-added="handleCommentAdded"
      />
    </div>
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
import CommentPanel from "./CommentPanel.vue";

const modules = [Navigation, Pagination, Mousewheel];
const videos = ref([]);
const userStore = useUserStore();
const showComments = ref(false);
const activeVideoId = ref(null);

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

  // 如果评论区开着，更新当前视频ID
  if (showComments.value) {
    const currentVideo = videos.value[currentIndex];
    if (currentVideo) {
      activeVideoId.value = currentVideo.id;
    }
  }

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

// 获取关注状态
const fetchFollowStatus = async () => {
  for (const video of videos.value) {
    try {
      const res = await axios.get("/api/follow/status", {
        params: { userId: userStore.userId, authorId: video.id }, // 暂时用 video.id 模拟作者ID
      });
      video.isFollowed = res.data.isFollowed;
    } catch (e) {
      console.error("获取关注状态失败", e);
    }
  }
};

// 获取点赞状态（在获取视频列表后调用）
const fetchLikeStatus = async () => {
  for (const video of videos.value) {
    try {
      const res = await axios.get("/api/like/status", {
        params: { userId: userStore.userId, videoId: video.id },
      });
      video.isLiked = res.data.isLiked;
      video.likeCount = res.data.likeCount;
    } catch (e) {
      console.error("获取点赞状态失败", e);
    }
  }
};

// 给视频补充模拟作者信息
const addMockAuthor = () => {
  videos.value.forEach((video) => {
    video.author = {
      name: "用户" + video.id,
      avatar: "/default-avatar.svg",
    };
  });
};

// 点赞/取消点赞
const toggleLike = async (video) => {
  try {
    const res = await axios.post("/api/like/toggle", {
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

// 关注/取关
const toggleFollow = async (video) => {
  try {
    const res = await axios.post("/api/follow/toggle", {
      userId: userStore.userId,
      authorId: video.id, // 暂时用 video.id 作为作者ID
    });
    if (res.data.code === 200) {
      video.isFollowed = res.data.isFollowed;
    }
  } catch (e) {
    console.error("关注失败", e);
  }
};

// 打开评论区
const openComments = (video) => {
  activeVideoId.value = video.id;
  showComments.value = true;
};

const handleCommentAdded = () => {
  const currentVideo = videos.value[currentIndex];
  if (currentVideo) {
    currentVideo.commentCount++;
  }
};

onMounted(async () => {
  try {
    const response = await getVideos(1, 10);
    videos.value = response.data.data.records;

    // 补充模拟作者信息
    addMockAuthor();

    await fetchLikeStatus(); // 新增

    await fetchFollowStatus();

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
  height: 100%;
  background-color: black;
  overflow: hidden;
  display: flex;
  margin: 0;
  padding: 0;
}

/* 视频区域 */
.video-area {
  flex: 1;
  height: 100%; /* 不是 100vh */
  position: relative;
  overflow: hidden;
  transition: flex-basis 0.3s ease;
}

.video-area.shrink {
  flex: 0 0 70%; /* 评论区打开时，视频区域宽度变为70% */
}

/* 评论区打开时，视频区域稍微缩小（视觉上被“挤”到左边） */
.video-area.shrink {
  flex: 0 0 70%; /* 评论区打开时，视频区占70%宽度 */
}

/* 评论区 */
.comment-section {
  width: 30%;
  height: 100%;
  background: #1e1e1e;
  border-left: 1px solid #333;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
  }
  to {
    transform: translateX(0);
  }
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
  overflow: hidden;
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
  align-items: center;
  gap: 20px;
  z-index: 10;
}

/* 作者头像区域 */
.author-area {
  position: relative;
  margin-bottom: 4px;
}

.author-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 2px solid white;
  object-fit: cover;
}

/* 关注按钮 */
.follow-btn {
  position: absolute;
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #ff4d4f;
  color: white;
  font-size: 12px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  user-select: none;
}

.follow-btn.followed {
  background: #666;
}

/* 操作按钮 */
.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: white;
  font-size: 22px;
  cursor: pointer;
  user-select: none;
  gap: 2px;
}

.action-num {
  font-size: 12px;
  color: #ccc;
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