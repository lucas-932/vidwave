package com.vidwave.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vidwave.entity.Video;
import com.vidwave.mapper.VideoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class VideoController {

    @Autowired
    private VideoMapper videoMapper;

    @GetMapping("/videos")
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // 简单分页（阶段一先不搞复杂）
        IPage<Video> videoPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        IPage<Video> result = videoMapper.selectPage(videoPage, null);

        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("message", "success");
        map.put("data", result);
        return map;
    }
}