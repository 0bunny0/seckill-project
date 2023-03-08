package com.powernode.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powernode.domain.Goods;
import com.powernode.mapper.GoodsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class MySQLToRedisUtil implements CommandLineRunner {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override   //当类启动时就会执行run方法
    public void run(String... args) throws Exception {
        /*查询参与秒杀的商品*/
        List<Goods> goodsList = goodsMapper.selectList(new LambdaQueryWrapper<Goods>()
                .eq(Goods::getStatus, 1)
                .eq(Goods::getSpike, 1));
        /*判断是否为空*/
        if(!CollectionUtils.isEmpty(goodsList)){
            /*保存商品到redis*/
            goodsList.forEach(goods -> {
                String key = "goods_stock:"+goods.getGoodsId();
                redisTemplate.opsForValue().set(key,goods.getTotalStocks().toString());
            });

            System.out.println("参与秒杀的商品已经写入redis");
        }
    }
}
