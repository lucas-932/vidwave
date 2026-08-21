<template>
  <div class="placeholder-page">
    <div class="placeholder-content">
      <span class="placeholder-icon">🚧</span>
      <h2>{{ title }}</h2>
      <p>功能开发中，敬请期待</p>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: "页面",
  },
});
</script>

<style scoped>
.placeholder-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-color: #111;
}

.placeholder-content {
  text-align: center;
  color: #999;
}

.placeholder-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 16px;
}

h2 {
  color: white;
  margin-bottom: 8px;
}
</style>