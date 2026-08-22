package com.vidwave.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vidwave.entity.User;
import com.vidwave.mapper.UserMapper;
import com.vidwave.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CaptchaController captchaController;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();

        // 1. 验证码校验（新增）
        String captchaKey = (String) body.get("captchaKey");
        int captchaAnswer = body.get("captchaAnswer") == null ? -1 : Integer.parseInt(body.get("captchaAnswer").toString());
        if (!captchaController.verify(captchaKey, captchaAnswer)) {
            result.put("code", 400);
            result.put("message", "验证码错误或已过期");
            return result;
        }

        // 2. 检查用户名密码是否为空（新增）
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            result.put("code", 400);
            result.put("message", "用户名和密码不能为空");
            return result;
        }

        // 3. 检查用户名是否已存在（保留）
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        if (userMapper.selectOne(wrapper) != null) {
            result.put("code", 400);
            result.put("message", "用户名已存在");
            return result;
        }

        // 4. 密码加密（保留）
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));

        // 5. 存入数据库（保留）
        userMapper.insert(user);

        result.put("code", 200);
        result.put("message", "注册成功");
        return result;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();

        // 1. 根据用户名查找用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        User existUser = userMapper.selectOne(wrapper);

        // 2. 用户不存在
        if (existUser == null) {
            result.put("code", 400);
            result.put("message", "用户名或密码错误");
            return result;
        }

        // 3. 密码比对
        boolean matches = passwordEncoder.matches(user.getPassword(), existUser.getPassword());
        if (!matches) {
            result.put("code", 400);
            result.put("message", "用户名或密码错误");
            return result;
        }

        // 4. 生成 JWT Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", existUser.getId());
        claims.put("username", existUser.getUsername());
        String token = jwtUtil.generateJwt(claims);

        // 5. 登录成功（阶段二先返回成功，阶段二后半段会引入 JWT Token）
        result.put("code", 200);
        result.put("message", "登录成功");
        result.put("token", token);
        result.put("avatarUrl", existUser.getAvatarUrl());
        result.put("userId", existUser.getId());
        return result;
    }

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(@RequestHeader("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();

        // 去掉 "Bearer " 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            Claims claims = jwtUtil.parseJwt(token);
            String username = claims.get("username", String.class);
            Long userId = claims.get("userId", Long.class);

            result.put("code", 200);
            result.put("message", "success");
            result.put("username", username);
            result.put("userId", userId);
        } catch (Exception e) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
        }

        return result;
    }
}
