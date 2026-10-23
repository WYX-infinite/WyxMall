package com.test.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 订单表
 * </p>
 *
 * @author wyx
 * @since 2026-08-18
 */
@Getter
@Setter
@ToString
@TableName("order_master")
@ApiModel(value = "OrderMaster对象", description = "订单表")
public class OrderMaster implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "order_id",type = IdType.ASSIGN_UUID)
    private String orderId;

    @ApiModelProperty("买家名字")
    @TableField("buyer_name")
    private String buyerName;

    @ApiModelProperty("买家电话")
    @TableField("buyer_phone")
    private String buyerPhone;


    @ApiModelProperty("买家地址")
    @TableField("buyer_address")
    private String buyerAddress;

    @ApiModelProperty("买家id")
    @TableField("buyer_openid")
    private Integer buyerOpenid;

    @ApiModelProperty("订单总金额")
    @TableField("order_amount")
    private BigDecimal orderAmount;

      /**
     * 订单状态，默认0新下单,1完成，2取消
     */
      @TableField("order_status")
    @ApiModelProperty("订单状态，默认0新下单,1完成，2取消")
    private Integer orderStatus;

      /**
     * 支付状态，默认0未支付，1已支付
     */
      @TableField("pay_status")
    @ApiModelProperty("支付状态，默认0未支付，1已支付")
    private Integer payStatus;

      /**
     * 创建时间
     */
      @ApiModelProperty("创建时间")
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date createTime;

      /**
     * 修改时间
     */
      @ApiModelProperty("修改时间")
    @TableField(value = "update_time",fill = FieldFill.INSERT)
    private Date updateTime;
}
