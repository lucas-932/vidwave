<template>
  <div class="comment-panel">
    <!-- 头部 -->
    <div class="comment-header">
      <span class="title">评论</span>
      <span class="close" @click="$emit('close')">✕</span>
    </div>

    <!-- 评论列表 -->
    <div class="comment-list">
      <div v-if="comments.length === 0" class="empty">暂无评论</div>
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <img src="/default-avatar.svg" class="comment-avatar" alt="头像" />
        <div class="comment-body">
          <div class="comment-name">
            用户{{ comment.userId }} · {{ formatTime(comment.createTime) }}
          </div>
          <div class="comment-content">{{ comment.content }}</div>
        </div>
      </div>
    </div>

    <!-- 输入框（固定在面板底部） -->
    <div class="comment-input">
      <input
        v-model="newComment"
        type="text"
        placeholder="发一条友善的评论"
        @keyup.enter="submitComment"
      />
      <button @click="submitComment" class="send-btn">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from "vue";
import axios from "axios";
import { useUserStore } from "../stores/userStore.js";

const props = defineProps({
  videoId: {
    type: Number,
    required: true,
  },
});

const emit = defineEmits(["close", "comment-added"]);
const comments = ref([]);
const newComment = ref("");
const userStore = useUserStore();

const fetchComments = async () => {
  try {
    const res = await axios.get("http://localhost:8080/api/comment/list", {
      params: { videoId: props.videoId },
    });
    if (res.data.code === 200) {
      comments.value = res.data.data;
    }
  } catch (e) {
    console.error("获取评论失败", e);
  }
};

// 格式化评论时间
const formatTime = (dateStr) => {
  if (!dateStr) return "";
  const date = new Date(dateStr);
  const now = new Date();
  const diff = Math.floor((now - date) / 1000); // 秒

  if (diff < 60) return "刚刚";
  if (diff < 3600) return Math.floor(diff / 60) + "分钟前";
  if (diff < 86400) return Math.floor(diff / 3600) + "小时前";
  return date.toLocaleDateString();
};

const submitComment = async () => {
  if (!newComment.value.trim()) {
    alert("评论内容不能为空");
    return;
  }

  try {
    const res = await axios.post("http://localhost:8080/api/comment/add", {
      videoId: props.videoId,
      userId: userStore.userId,
      content: newComment.value,
    });

    if (res.data.code === 200) {
      newComment.value = "";
      await fetchComments();
      emit("comment-added");
    }
  } catch (e) {
    console.error("发表评论失败", e);
  }
};

onMounted(() => {
  fetchComments();
});

// 当视频切换时，重新加载评论
watch(
  () => props.videoId,
  () => {
    fetchComments();
  }
);
</script>

<style scoped>
.comment-panel {
  width: 100%;
  height: 100%;
  background: #1e1e1e;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative; /* 新增：为内部定位提供参考 */
}

.comment-header {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  border-bottom: 1px solid #333;
  flex-shrink: 0;
}

.comment-header .title {
  color: white;
  font-size: 16px;
  font-weight: 600;
}

.comment-header .close {
  margin-left: auto;
  color: #999;
  cursor: pointer;
}

.comment-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  min-height: 0;
}

.empty {
  color: #666;
  text-align: center;
  padding-top: 40px;
}

.comment-item {
  display: flex;
  margin-bottom: 16px;
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  margin-right: 12px;
  object-fit: cover;
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
}

.comment-name {
  color: #999;
  font-size: 12px;
  margin-bottom: 4px;
}

.comment-content {
  color: white;
  font-size: 14px;
}

/* 输入框固定在底部 */
.comment-input {
  display: flex;
  border-top: 1px solid #444;
  padding: 12px;
  background: #161616;
  flex-shrink: 0;
}

.comment-input input {
  flex: 1;
  background: #2a2a2a;
  border: none;
  border-radius: 20px;
  padding: 8px 14px;
  color: white;
  outline: none;
  font-size: 13px;
}

.send-btn {
  background: #4a6cf7;
  color: white;
  border: none;
  border-radius: 20px;
  padding: 0 16px;
  margin-left: 8px;
  cursor: pointer;
}
</style>