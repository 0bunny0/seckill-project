package com.powernode.service.impl;

import com.powernode.mapper.GoodsMapper;
import com.powernode.service.OrderService;
import io.lettuce.core.RedisClient;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powernode.domain.Order;
import com.powernode.mapper.OrderMapper;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 秒杀业务
     * 插入订单表
     * @param userId
     * @param goodsId
     * @return
     */

    @Transactional
    public int doProcessSeckill1(Integer userId, Integer goodsId) {
        int count = goodsMapper.updateStock(goodsId);
        if(count > 0) {
            /*插入订单*/
            count = writeOrder(userId,goodsId);
        }
        return count;
    }

    /*使用redisson实现分布式锁*/
    @Transactional
    @Override
    public int doProcessSeckill(Integer userId, Integer goodsId){
        /* 获取锁 -- redis + lua */
        RLock lock = redissonClient.getLock("goods_lock:"+goodsId);
        /* 设置锁超时时间为30s */
        lock.lock(30, TimeUnit.SECONDS);
        try{
            int count = goodsMapper.updateStock(goodsId);
            if(count > 0) {
                /*插入订单*/
                count = writeOrder(userId,goodsId);
            }
            return count;
        }finally {
            /*释放锁*/
            lock.unlock();
        }
    }

    /*使用redis实现分布式锁*/
    @Transactional
    public void doProcessSecKill3(Integer userId, Integer goodsId){
        /*询问redis是否有goodsId对应的锁*/
        Boolean flag = redisTemplate.opsForValue().setIfAbsent("goods_lock:"+goodsId,"");
        /*设置成功，说明没有该商品id的锁*/
        if(flag){
            /*更新数据库库存*/
            try{
                int count = goodsMapper.updateStock(goodsId);
                if(count > 0) {
                    /*插入订单*/
                    writeOrder(userId,goodsId);
                }
            }finally {
                /*释放锁*/
                redisTemplate.delete("goods_lock:"+goodsId);
            }
        }
    }

    /*redis锁优化*/
    @Transactional
    public void doProcessSecKill4(Integer userId, Integer goodsId){
        /*自旋锁时间*/
        long current = 0L;
        /* 根据goodsId去redis获取锁 -- 执行redis底层setnx命令，过期时间30s */
        while(current <= 1000){
            boolean flag = redisTemplate.opsForValue().setIfAbsent("goods_lock:" + goodsId, "1",
                    Duration.ofSeconds(30));
            /*设置成功，说明没有该商品id的锁*/
            if(flag){
                /*更新数据库库存*/
                try{
                    /*获得了锁，则修改数据库*/
                    int count = goodsMapper.updateStock(goodsId);
                    if(count > 0) {
                        /*插入订单*/
                        writeOrder(userId,goodsId);
                    }
                }finally {
                    /*释放锁*/
                    redisTemplate.delete("goods_lock:"+goodsId);
                }
            }else{
                /* 没有获取到锁,停留一下继续获取锁(自旋锁) */
                try{
                    Thread.sleep(200);
                    current += 200;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    /*插入订单*/
    private int writeOrder(Integer uesrId,Integer goodsId){
        Order order = new Order();
        order.setCreatetime(new Date());
        order.setGoodsid(goodsId);
        order.setUserid(uesrId);
        return orderMapper.insert(order);
    }
}
