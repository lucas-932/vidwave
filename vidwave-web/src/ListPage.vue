<template>
  <div class="list-page">
    <h1>视频列表</h1>
    <ul>
      <li v-for="video in videos" :key="video.id">
        {{ video.title }}
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getVideos } from './api/index.js'

const videos = ref([])

onMounted(async () => {
  const response = await getVideos(1, 10)
  videos.value = response.data.data.records
})
</script>

<style scoped>
.list-page {
  padding: 20px;
  background: white;
  color: black;
  min-height: 100vh;
}
</style>

