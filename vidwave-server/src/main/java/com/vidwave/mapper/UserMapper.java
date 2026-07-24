package com.vidwave.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vidwave.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
