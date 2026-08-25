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
              @click="togglePlay(video)"
            />
            <!-- 播放状态图标（暂停时显示播放图标） -->
            <div
              v-if="!video.isPlaying"
              class="play-overlay"
              @click.stop="togglePlay(video)"
            >
              <span class="play-icon">▶</span>
            </div>
            <!-- 右侧操作栏 -->
            <div class="actions">
              <!-- 作者头像 + 关注按钮 -->
              <div class="author-area">
                <img
                  :src="video.author?.avatar || '/default-avatar.svg'"
                  class="author-avatar"
                  alt="作者头像"
                  @click="goToUserPage(video)"
                  style="cursor: pointer"
                />
                <div
                  class="follow-btn"
                  :class="{ followed: video.isFollowed }"
                  @click.stop="toggleFollow(video)"
                >
                  {{ video.isFollowed ? "✓" : "+" }}
                </div>
                <div class="author-name">
                  {{ video.author?.name || "用户" }}
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
            <div class="video-info">
              <!-- 标题 -->
              <div class="title">{{ video.title }}</div>

              <!-- 标签 -->
              <div v-if="video.tags" class="tags">
                <span
                  v-for="tag in video.tags.split(',')"
                  :key="tag"
                  class="tag"
                  @click="handleTagClick(tag)"
                >
                  #{{ tag }}
                </span>
              </div>

              <!-- 发布时间 -->
              <div class="time">{{ formatTime(video.createTime) }}</div>
            </div>
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
import { Swiper, SwiperSlide } from "swiper/vue";
import { Navigation, Pagination, Mousewheel } from "swiper/modules";
import "swiper/css";
import { getVideos } from "../api/index.js";
import { useUserStore } from "../stores/userStore.js";
import axios from "axios";
import CommentPanel from "./CommentPanel.vue";
import { useRouter } from "vue-router";
import { ref, onMounted, onActivated, nextTick } from "vue";

defineOptions({
  name: "RecommendPage",
});

const modules = [Navigation, Pagination, Mousewheel];
const videos = ref([]);
const userStore = useUserStore();
const showComments = ref(false);
const activeVideoId = ref(null);
const router = useRouter();

const videoRefs = {};
const setVideoRef = (el, id) => {
  if (el) videoRefs[id] = el;
};

let swiperInstance = null;
let currentIndex = 0;

const goToUserPage = (video) => {
  if (video.userId) {
    router.push(`/user/${video.userId}`);
  }
};

const onSwiper = (swiper) => {
  swiperInstance = swiper;
  nextTick(() => playCurrentVideo());
};

const onSlideChange = (swiper) => {
  Object.values(videoRefs).forEach((v) => v.pause());
  videos.value.forEach((v) => (v.isPlaying = false));

  currentIndex = swiper.activeIndex;

  // 当前视频从头播放
  const currentVideo = videos.value[currentIndex];
  if (currentVideo) {
    currentVideo.isPlaying = true;
    const videoEl = videoRefs[currentVideo.id];
    if (videoEl) videoEl.currentTime = 0;
  }

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
        params: { userId: userStore.userId, authorId: video.userId },
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

const fetchAuthors = async () => {
  for (const video of videos.value) {
    if (video.userId) {
      try {
        const res = await axios.get(`/api/user/${video.userId}`);
        if (res.data.code === 200) {
          video.author = {
            name: res.data.data.username,
            avatar: res.data.data.avatarUrl || "/default-avatar.svg",
          };
        }
      } catch (e) {
        console.error("获取作者信息失败", e);
      }
    } else {
      video.author = {
        name: "未知用户",
        avatar: "/default-avatar.svg",
      };
    }
  }
};

//点击暂定/播放视频
const togglePlay = (video) => {
  const videoEl = videoRefs[video.id];
  if (!videoEl) return;

  if (videoEl.paused) {
    videoEl.play().catch((err) => console.warn("播放失败：", err));
    video.isPlaying = true;
  } else {
    videoEl.pause();
    video.isPlaying = false;
  }
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
      authorId: video.userId,
    });
    if (res.data.code === 200) {
      video.isFollowed = res.data.isFollowed;
    }
  } catch (e) {
    console.error("关注失败", e);
  }
};

// 点击标签（以后跳转到搜索页）
const handleTagClick = (tag) => {
  console.log("点击了标签：" + tag);
  // 以后这里写：router.push('/search?keyword=' + tag)
};

// 格式化发布时间
const formatTime = (dateStr) => {
  if (!dateStr) return "";

  const date = new Date(dateStr);
  const now = new Date();
  const diff = Math.floor((now - date) / 1000);

  if (diff < 60) return "刚刚";
  if (diff < 3600) return Math.floor(diff / 60) + "分钟前";
  if (diff < 86400) return Math.floor(diff / 3600) + "小时前";
  if (diff < 2592000) return Math.floor(diff / 86400) + "天前";
  return date.toLocaleDateString();
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

    // 给每个视频添加播放状态
    videos.value.forEach((v) => {
      v.isPlaying = true; // 默认自动播放，设为播放中
    });

    await fetchAuthors();

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

onActivated(() => {
  nextTick(() => {
    playCurrentVideo();
  });
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
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.author-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 2px solid white;
  object-fit: cover;
  cursor: pointer;
}
.author-name {
  color: white;
  font-size: 11px;
  text-align: center;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
  max-width: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 关注按钮 */
.follow-btn {
  width: 20px;
  height: 20px;
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
  flex-shrink: 0;

  /* 关键：让按钮向上压住头像底部 */
  margin-top: -18px;
  border: 1.5px solid #111; /* 和背景同色，看起来像被头像切了一刀 */
  z-index: 2;
}

.follow-btn.followed {
  background: #666;
}

.author-name {
  color: white;
  font-size: 11px;
  margin-top: 4px;
  text-align: center;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
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

/* 底部 */
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
  margin-bottom: 6px;
}

.tag {
  color: rgba(255, 255, 255, 0.75); /* 半透明白，不抢眼 */
  font-size: 12px;
  cursor: pointer;
  transition: color 0.15s ease, background-color 0.15s ease;
  padding: 2px 8px;
  border-radius: 12px;
}

.tag:hover {
  color: #ffffff; /* 悬停变纯白 */
  background-color: rgba(255, 255, 255, 0.15); /* 加个浅色底 */
}

.time {
  color: #999;
  font-size: 11px;
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

.play-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 20;
  cursor: pointer;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 50%;
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.play-icon {
  color: white;
  font-size: 40px;
  margin-left: 8px; /* 让三角视觉居中 */
}
</style>