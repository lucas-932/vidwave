package com.vidwave.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vidwave.entity.Video;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VideoMapper extends BaseMapper<Video> {
}
