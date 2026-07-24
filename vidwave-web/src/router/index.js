import { createRouter, createWebHistory } from 'vue-router'

// 1. 在这里导入你未来的页面组件
//    我们先从占位组件开始，让你的项目能跑通
import RecommendPage from '../views/RecommendPage.vue'
import FollowPage from '../views/FollowPage.vue'
import ProfilePage from '../views/ProfilePage.vue'

// 2. 定义路由规则：URL路径和组件的对应关系
const routes = [
  { path: '/', redirect: '/recommend' }, // 访问根路径，自动跳转到“推荐”页
  { path: '/recommend', component: RecommendPage },
  { path: '/follow', component: FollowPage },
  { path: '/profile', component: ProfilePage },
]

// 3. 创建路由实例
const router = createRouter({
  history: createWebHistory(), // 使用HTML5 History模式，URL里没有‘#’号，更干净
  routes,
})

export default router