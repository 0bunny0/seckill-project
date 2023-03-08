package com.powernode.controller;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SeckillController {

    @Autowired
    private BitMapBloomFilter bitMapBloomFilter;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;


    @GetMapping("/doSeckill")
    public String doSeckill(Integer goodsId,Integer userId){
        /* 一个用户同一个商品只能抢购一次 -- userId -- goodsId */
        String seckillId = userId + "-" + goodsId;
        /* 使用布隆过滤器检查一下当前用户是否已经参与抢购 */
        if(bitMapBloomFilter.contains(seckillId)){
            return "你已经参与过该商品的抢购，请参与其他商品(*^▽^*)";
        }
        /*没有参与过抢购，再添加到布隆过滤器*/
        bitMapBloomFilter.add(seckillId);
        /* 从redis中扣除库存 -- redis自带命令 incr, descr */

        Long count = redisTemplate.opsForValue().decrement("goods_stock:"+goodsId);
        /*预留30%的用户获得抢购资格*/
        if(count < -3){
            return "此商品抢购完了，下次手速快点";
        }
        /*执行到此，在redis中获取了抢购资格 -- 写入消息队列*/
        SendResult result = rocketMQTemplate.syncSend("seckill", seckillId);
        System.out.println(result);
        return "正在排队抢购中，请耐心等待...";
    }
}
