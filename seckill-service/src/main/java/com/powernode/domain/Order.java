package com.powernode.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "`order`")
public class Order {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "userid")
    private Integer userid;

    @TableField(value = "goodsid")
    private Integer goodsid;

    @TableField(value = "createtime")
    private Date createtime;
}