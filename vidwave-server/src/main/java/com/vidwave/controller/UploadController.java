package com.vidwave.controller;

import com.vidwave.entity.User;
import com.vidwave.entity.Video;
import com.vidwave.mapper.UserMapper;
import com.vidwave.mapper.VideoMapper;
import com.vidwave.util.OssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "http://localhost:5173")
public class UploadController {

    @Autowired
    private OssUtil ossUtil;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/video")
    public Map<String, Object> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "cover", required = false) MultipartFile cover,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "userId", required = false) Long userId) {

        Map<String, Object> result = new HashMap<>();

        // 1. 上传视频到 OSS
        String videoUrl = ossUtil.uploadFile(file, "videos");
        if (videoUrl == null) {
            result.put("code", 500);
            result.put("message", "视频上传失败");
            return result;
        }

        // 2. 上传封面（如果有）
        String coverUrl = null;
        if (cover != null && !cover.isEmpty()) {
            coverUrl = ossUtil.uploadFile(cover, "covers");
        }

        // 3. 写入数据库
        Video video = new Video();
        video.setTitle(title == null || title.isEmpty() ? "无标题" : title);
        video.setVideoUrl(videoUrl);
        video.setCoverUrl(coverUrl == null ? "" : coverUrl);
        video.setTags(tags == null ? "" : tags);
        video.setDuration(0); // 先设为0，以后用FFmpeg提取
        video.setLikeCount(0);
        video.setCommentCount(0);
        video.setUserId(userId);
        videoMapper.insert(video);

        result.put("code", 200);
        result.put("message", "上传成功");
        result.put("videoId", video.getId());
        return result;
    }

    @PostMapping("/avatar")
    public Map<String, Object> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) {

        Map<String, Object> result = new HashMap<>();

        // 1. 上传到 OSS
        String avatarUrl = ossUtil.uploadFile(file, "avatars");
        if (avatarUrl == null) {
            result.put("code", 500);
            result.put("message", "头像上传失败");
            return result;
        }

        // 2. 更新用户表
        User user = userMapper.selectById(userId);
        if (user == null) {
            result.put("code", 400);
            result.put("message", "用户不存在");
            return result;
        }
        user.setAvatarUrl(avatarUrl);
        userMapper.updateById(user);

        result.put("code", 200);
        result.put("message", "头像上传成功");
        result.put("avatarUrl", avatarUrl);
        return result;
    }
}