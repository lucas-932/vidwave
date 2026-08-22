package com.vidwave.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/follow")
@CrossOrigin(origins = "http://localhost:5173")
public class FollowController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 关注/取消关注
    @PostMapping("/toggle")
    public Map<String, Object> toggleFollow(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Long authorId = Long.valueOf(body.get("authorId").toString());

        Map<String, Object> result = new HashMap<>();
        String followKey = "follow:" + userId + ":" + authorId;

        Boolean isFollowed = redisTemplate.hasKey(followKey);
        if (isFollowed != null && isFollowed) {
            redisTemplate.delete(followKey);
            result.put("code", 200);
            result.put("message", "取消关注");
            result.put("isFollowed", false);
        } else {
            redisTemplate.opsForValue().set(followKey, "1");
            result.put("code", 200);
            result.put("message", "关注成功");
            result.put("isFollowed", true);
        }
        return result;
    }

    // 获取关注状态
    @GetMapping("/status")
    public Map<String, Object> getStatus(@RequestParam Long userId, @RequestParam Long authorId) {
        Map<String, Object> result = new HashMap<>();
        String followKey = "follow:" + userId + ":" + authorId;
        Boolean isFollowed = redisTemplate.hasKey(followKey);
        result.put("code", 200);
        result.put("isFollowed", isFollowed != null && isFollowed);
        return result;
    }
}