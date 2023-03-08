package com.powernode.listener;

import com.powernode.service.OrderService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(topic = "seckill",
consumerGroup = "seckill-consumer-group")
public class GoodsListener implements RocketMQListener<String> {

    @Autowired
    private OrderService orderService;

    /*监听队列中所有抢购的信息*/
    @Override
    public void onMessage(String s) {

        /*userId - goodsId拆分*/
        String[] split = s.split("-");
        Integer userId = Integer.parseInt(split[0]);
        Integer goodsId = Integer.parseInt(split[1]);

        /*执行订单业务*/
        int count = orderService.doProcessSeckill(userId,goodsId);
        if(count>0)
        {
            System.out.println("用户:"+userId+"，成功抢购了商品："+goodsId);
        }
        else
        {
            System.out.println("用户:"+userId+"，抢购商品："+goodsId+"失败了");
        }

    }
}
