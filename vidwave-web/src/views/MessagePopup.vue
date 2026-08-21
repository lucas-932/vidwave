<template>
  <div class="message-popup">
    <!-- 头部 -->
    <div class="message-header">
      <span v-if="!activeChat" class="header-title">消息</span>
      <template v-else>
        <span class="back-btn" @click="activeChat = null">←</span>
        <span class="header-title">{{ activeChat.name }}</span>
      </template>
      <span class="close-btn" @click="$emit('close')">✕</span>
    </div>

    <!-- 会话列表 -->
    <div v-if="!activeChat" class="chat-list">
      <div
        v-for="chat in chatList"
        :key="chat.id"
        class="chat-item"
        @click="activeChat = chat"
      >
        <img :src="chat.avatar" class="chat-avatar" alt="头像" />
        <div class="chat-info">
          <span class="chat-name">{{ chat.name }}</span>
          <span class="chat-preview">{{ chat.preview }}</span>
        </div>
      </div>
    </div>

    <!-- 聊天窗口 -->
    <div v-else class="chat-window">
      <div
        v-for="(msg, index) in activeChat.messages"
        :key="index"
        class="message-row"
        :class="msg.from === 'me' ? 'from-me' : 'from-other'"
      >
        {{ msg.text }}
      </div>
      <div class="chat-input">
        <input type="text" placeholder="发消息..." />
        <button class="send-btn">发送</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";

const chatList = [
  {
    id: 1,
    name: "微澜小助手",
    avatar: "https://via.placeholder.com/40",
    preview: "欢迎来到微澜！",
    messages: [
      { from: "other", text: "欢迎来到微澜！" },
      { from: "me", text: "你好呀" },
    ],
  },
  {
    id: 2,
    name: "系统通知",
    avatar: "https://via.placeholder.com/40",
    preview: "你的视频通过了审核",
    messages: [{ from: "other", text: "你的视频《测试视频》已通过审核。" }],
  },
];

const activeChat = ref(null);

defineEmits(["close"]);
</script>

<style scoped>
.message-popup {
  width: 320px;
  height: 460px;
  background: #1e1e1e;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.5);
}

.message-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #333;
}

.back-btn {
  cursor: pointer;
  margin-right: 12px;
  color: #ccc;
}

.header-title {
  color: white;
  font-weight: 600;
}

.close-btn {
  margin-left: auto;
  cursor: pointer;
  color: #999;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.chat-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  cursor: pointer;
}

.chat-item:hover {
  background: #2a2a2a;
}

.chat-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 12px;
  object-fit: cover;
}

.chat-info {
  display: flex;
  flex-direction: column;
}

.chat-name {
  color: white;
  font-size: 14px;
}

.chat-preview {
  color: #999;
  font-size: 12px;
  margin-top: 2px;
}

.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px;
  overflow-y: auto;
}

.message-row {
  max-width: 70%;
  padding: 8px 12px;
  border-radius: 12px;
  margin-bottom: 8px;
  font-size: 14px;
}

.from-other {
  background: #333;
  color: white;
  align-self: flex-start;
}

.from-me {
  background: #4a6cf7;
  color: white;
  align-self: flex-end;
}

.chat-input {
  display: flex;
  border-top: 1px solid #333;
  padding: 10px;
}

.chat-input input {
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