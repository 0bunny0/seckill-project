package com.powernode;

import org.mybatis.spring.annotation.MapperScan;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
@MapperScan(basePackages = {"com.powernode.mapper"})
public class SeckillServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeckillServiceApplication.class, args);
    }

    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        //设置单机服务器地址
        config.useSingleServer().setAddress("redis://localhost:6379");
        return Redisson.create(config);
    }

}
