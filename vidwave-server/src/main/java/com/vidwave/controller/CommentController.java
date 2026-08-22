package com.vidwave.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vidwave.entity.Comment;
import com.vidwave.entity.Video;
import com.vidwave.mapper.CommentMapper;
import com.vidwave.mapper.VideoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comment")
@CrossOrigin(origins = "http://localhost:5173")
public class CommentController {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private VideoMapper videoMapper;

    // 获取某个视频的评论列表
    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam Long videoId) {
        Map<String, Object> result = new HashMap<>();

        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getVideoId, videoId)
                .orderByDesc(Comment::getCreateTime);

        List<Comment> comments = commentMapper.selectList(wrapper);

        result.put("code", 200);
        result.put("message", "success");
        result.put("data", comments);
        return result;
    }

    // 发表评论
    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Comment comment) {
        Map<String, Object> result = new HashMap<>();

        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            result.put("code", 400);
            result.put("message", "评论内容不能为空");
            return result;
        }

        commentMapper.insert(comment);

        Video video = videoMapper.selectById(comment.getVideoId());
        if (video != null) {
            video.setCommentCount((video.getCommentCount() == null ? 0 : video.getCommentCount()) + 1);
            videoMapper.updateById(video);
        }

        result.put("code", 200);
        result.put("message", "评论成功");
        return result;
    }
}