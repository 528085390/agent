package com.li.liaiagent.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.li.liaiagent.pojo.MessageMemory;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;


@Mapper
public interface MessageMemoryMapper extends BaseMapper<MessageMemory> {
}
