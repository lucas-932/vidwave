<template>
  <div class="upload-page">
    <div class="upload-content">
      <span class="upload-icon">📤</span>
      <h2>上传视频</h2>
      <p>阶段三将在这里实现视频上传和转码功能</p>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.upload-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-color: #111;
}

.upload-content {
  text-align: center;
  color: #999;
}

.upload-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 16px;
}

h2 {
  color: white;
  margin-bottom: 8px;
}
</style>