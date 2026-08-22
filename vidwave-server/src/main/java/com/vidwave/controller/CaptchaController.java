package com.vidwave.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/captcha")
@CrossOrigin(origins = "http://localhost:5173")
public class CaptchaController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 生成算术验证码
    @GetMapping("/generate")
    public Map<String, Object> generate() {
        Map<String, Object> result = new HashMap<>();
        Random random = new Random();
        int num1 = random.nextInt(10) + 1; // 1-10
        int num2 = random.nextInt(10) + 1;
        boolean isAdd = random.nextBoolean();

        String question;
        int answer;
        if (isAdd) {
            question = num1 + " + " + num2 + " = ?";
            answer = num1 + num2;
        } else {
            // 确保减法不出现负数
            if (num1 < num2) {
                int temp = num1;
                num1 = num2;
                num2 = temp;
            }
            question = num1 + " - " + num2 + " = ?";
            answer = num1 - num2;
        }

        // 唯一 key，存答案到 Redis，60秒过期
        String captchaKey = "captcha:" + System.currentTimeMillis() + random.nextInt(1000);
        redisTemplate.opsForValue().set(captchaKey, String.valueOf(answer), 60, TimeUnit.SECONDS);

        result.put("code", 200);
        result.put("message", "success");
        result.put("captchaKey", captchaKey);
        result.put("question", question);
        return result;
    }

    // 验证答案（供其他Controller调用）
    public boolean verify(String captchaKey, int userAnswer) {
        if (captchaKey == null || captchaKey.isEmpty()) {
            return false;
        }
        String correctAnswer = redisTemplate.opsForValue().get(captchaKey);
        if (correctAnswer == null) {
            return false;
        }
        // 验证后立即删除，防止重复使用
        redisTemplate.delete(captchaKey);
        try {
            return Integer.parseInt(correctAnswer) == userAnswer;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}