package com.vidwave.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/like")
@CrossOrigin(origins = "http://localhost:5173")
public class LikeController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 点赞/取消点赞
    @PostMapping("/toggle")
    public Map<String, Object> toggleLike(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Long videoId = Long.valueOf(body.get("videoId").toString());

        Map<String, Object> result = new HashMap<>();
        String likeKey = "like:" + userId + ":" + videoId;

        Boolean isLiked = redisTemplate.hasKey(likeKey);
        if (isLiked != null && isLiked) {
            // 取消点赞
            redisTemplate.delete(likeKey);
            redisTemplate.opsForValue().decrement("video:likes:" + videoId);
            result.put("code", 200);
            result.put("message", "取消点赞");
            result.put("isLiked", false);
        } else {
            // 点赞
            redisTemplate.opsForValue().set(likeKey, "1");
            redisTemplate.opsForValue().increment("video:likes:" + videoId);
            result.put("code", 200);
            result.put("message", "点赞成功");
            result.put("isLiked", true);
        }
        return result;
    }

    // 获取点赞状态和点赞数
    @GetMapping("/status")
    public Map<String, Object> getStatus(@RequestParam Long userId, @RequestParam Long videoId) {
        Map<String, Object> result = new HashMap<>();
        String likeKey = "like:" + userId + ":" + videoId;
        Boolean isLiked = redisTemplate.hasKey(likeKey);
        String countStr = redisTemplate.opsForValue().get("video:likes:" + videoId);
        Long likeCount = countStr == null ? 0L : Long.parseLong(countStr);

        result.put("code", 200);
        result.put("isLiked", isLiked != null && isLiked);
        result.put("likeCount", likeCount);
        return result;
    }
}