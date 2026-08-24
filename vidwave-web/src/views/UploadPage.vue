<template>
  <div class="upload-page">
    <div class="upload-card">
      <h2>上传视频</h2>
      <p class="subtitle">分享你的精彩瞬间</p>

      <!-- 标题输入 -->
      <input
        v-model="title"
        type="text"
        placeholder="请输入视频标题"
        class="input"
      />

      <!-- 视频选择 -->
      <label class="file-label">
        <span>选择视频文件</span>
        <input
          type="file"
          accept="video/*"
          @change="handleVideoChange"
          hidden
        />
      </label>
      <p v-if="videoFile" class="file-name">已选择：{{ videoFile.name }}</p>

      <!-- 封面上传（可选） -->
      <label class="file-label">
        <span>选择封面图（可选）</span>
        <input
          type="file"
          accept="image/*"
          @change="handleCoverChange"
          hidden
        />
      </label>
      <p v-if="coverFile" class="file-name">已选择：{{ coverFile.name }}</p>

      <!-- 预设标签（已从 label 里移出） -->
      <div class="tag-group">
        <span
          v-for="tag in presetTags"
          :key="tag"
          class="tag-btn"
          :class="{ selected: selectedTags.includes(tag) }"
          @click="toggleTag(tag)"
        >
          {{ tag }}
        </span>
      </div>

      <!-- 自定义补充标签 -->
      <input
        v-model="customTag"
        type="text"
        placeholder="补充标签（可选，用逗号分隔）"
        class="input"
      />

      <!-- 上传按钮 -->
      <button @click="handleUpload" class="btn" :disabled="uploading">
        {{ uploading ? "上传中..." : "上传" }}
      </button>

      <!-- 提示 -->
      <p v-if="message" class="message">{{ message }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import axios from "axios";
import { useUserStore } from "../stores/userStore.js";

const title = ref("");
const videoFile = ref(null);
const coverFile = ref(null);
const uploading = ref(false);
const message = ref("");
const userStore = useUserStore();

const presetTags = [
  "搞笑",
  "美食",
  "猫",
  "旅行",
  "游戏",
  "日常",
  "运动",
  "音乐",
  "知识",
  "其他",
];
const selectedTags = ref([]);
const customTag = ref("");

const toggleTag = (tag) => {
  if (selectedTags.value.includes(tag)) {
    selectedTags.value = selectedTags.value.filter((t) => t !== tag);
  } else {
    selectedTags.value.push(tag);
  }
};

const handleVideoChange = (event) => {
  videoFile.value = event.target.files[0];
};

const handleCoverChange = (event) => {
  coverFile.value = event.target.files[0];
};

const handleUpload = async () => {
  if (!videoFile.value) {
    message.value = "请先选择视频文件";
    return;
  }

  const formData = new FormData();
  formData.append("file", videoFile.value);
  formData.append("title", title.value);
  formData.append("userId", userStore.userId);

  // 合并标签：预设 + 自定义
  const finalTags = [...selectedTags.value];
  if (customTag.value.trim()) {
    const customs = customTag.value
      .split(",")
      .map((s) => s.trim())
      .filter((s) => s);
    finalTags.push(...customs);
  }
  formData.append("tags", finalTags.join(","));

  if (coverFile.value) {
    formData.append("cover", coverFile.value);
  }

  uploading.value = true;
  message.value = "";

  try {
    const res = await axios.post(
      "http://localhost:8080/api/upload/video",
      formData,
      { headers: { "Content-Type": "multipart/form-data" } }
    );

    if (res.data.code === 200) {
      message.value = "上传成功！";
      title.value = "";
      videoFile.value = null;
      coverFile.value = null;
      selectedTags.value = []; // 清空已选标签
      customTag.value = ""; // 清空自定义标签
    } else {
      message.value = res.data.message;
    }
  } catch (e) {
    console.error(e);
    message.value = "上传失败，请重试";
  } finally {
    uploading.value = false;
  }
};
</script>
<style scoped>
.upload-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-color: #111;
  padding: 20px;
}

.upload-card {
  background: #1e1e1e;
  padding: 30px;
  border-radius: 12px;
  width: 400px;
  text-align: center;
}

h2 {
  color: white;
  margin-bottom: 5px;
}

.subtitle {
  color: #888;
  font-size: 14px;
  margin-bottom: 20px;
}

.input {
  width: 100%;
  padding: 10px;
  margin-bottom: 15px;
  background: #2a2a2a;
  border: 1px solid #444;
  border-radius: 6px;
  color: white;
  outline: none;
  box-sizing: border-box;
}

.file-label {
  display: block;
  background: #2a2a2a;
  border: 1px dashed #666;
  border-radius: 6px;
  padding: 12px;
  color: #ccc;
  cursor: pointer;
  margin-bottom: 10px;
}

.file-label:hover {
  background: #333;
}

.file-name {
  color: #4a6cf7;
  font-size: 12px;
  margin-bottom: 10px;
}

.btn {
  width: 100%;
  padding: 12px;
  background: #4a6cf7;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
}

.btn:disabled {
  background: #666;
  cursor: not-allowed;
}

.message {
  margin-top: 15px;
  color: #4a6cf7;
  font-size: 14px;
}

.tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 15px;
}

.tag-btn {
  padding: 6px 14px;
  background: #2a2a2a;
  border: 1px solid #444;
  border-radius: 20px;
  color: #ccc;
  cursor: pointer;
  font-size: 13px;
  user-select: none;
  transition: all 0.15s;
}

.tag-btn:hover {
  background: #333;
}

.tag-btn.selected {
  background: #4a6cf7;
  border-color: #4a6cf7;
  color: white;
}
</style>