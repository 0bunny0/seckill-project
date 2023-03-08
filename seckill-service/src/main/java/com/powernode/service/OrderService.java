package com.powernode.service;

import com.powernode.domain.Order;
import com.baomidou.mybatisplus.extension.service.IService;
public interface OrderService extends IService<Order>{
    /**
     * 秒杀业务
     * @param userId
     * @param goodsId
     * @return
     */
    int doProcessSeckill(Integer userId, Integer goodsId);
}
