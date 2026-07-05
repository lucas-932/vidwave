import axios from 'axios'

const request = axios.create({
  baseURL: 'http://localhost:8080',  // 指向你的后端
  timeout: 10000
})

// 获取视频列表
export function getVideos(page = 1, size = 5) {
  return request.get('/api/videos', {
    params: { page, size }
  })
}