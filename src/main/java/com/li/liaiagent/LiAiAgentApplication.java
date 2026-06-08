package com.li.liaiagent;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.li.liaiagent.mapper")

public class  LiAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiAiAgentApplication.class, args);
    }

}
