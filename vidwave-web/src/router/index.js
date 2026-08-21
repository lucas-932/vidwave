import { createRouter, createWebHistory } from 'vue-router'
import RecommendPage from '../views/RecommendPage.vue'
import FollowPage from '../views/FollowPage.vue'
import ProfilePage from '../views/ProfilePage.vue'
import PlaceholderPage from '../views/PlaceholderPage.vue'
import UploadPage from '../views/UploadPage.vue'

const routes = [
  { path: '/', redirect: '/recommend' },
  { path: '/recommend', component: RecommendPage },
  { path: '/follow', component: FollowPage },
  { path: '/profile', component: ProfilePage },
  { path: '/upload', component: UploadPage },
  // 新增占位路由
  { path: '/featured', component: PlaceholderPage, props: { title: '精选' } },
  { path: '/AiVidwave', component: PlaceholderPage, props: { title: 'AI微澜' } },
  { path: '/friends', component: PlaceholderPage, props: { title: '朋友' } },
  { path: '/live', component: PlaceholderPage, props: { title: '直播' } },
  { path: '/cinema', component: PlaceholderPage, props: { title: '放映厅' } },
  { path: '/drama', component: PlaceholderPage, props: { title: '短剧' } },
  { path: '/games', component: PlaceholderPage, props: { title: '小游戏' } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router