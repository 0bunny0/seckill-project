package com.powernode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powernode.domain.Goods;

public interface GoodsMapper extends BaseMapper<Goods> {
    /*更新库存*/
    int updateStock(Integer goodsId);
}