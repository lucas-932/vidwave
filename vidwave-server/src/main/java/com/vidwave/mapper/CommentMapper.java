package com.vidwave.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vidwave.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}