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

/**
 * <p>
 * 订单详情表
 * </p>
 *
 * @author wyx
 * @since 2026-08-18
 */
@Getter
@Setter
@ToString
@TableName("order_detail")
@ApiModel(value = "OrderDetail对象", description = "订单详情表")
public class OrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "detail_id", type = IdType.ASSIGN_UUID)
    private String detailId;

    @TableField("order_id")
    private String orderId;

    @TableField("product_id")
    private Integer productId;

      /**
     * 商品名称
     */
      @ApiModelProperty("商品名称")
    @TableField("product_name")
    private String productName;

      /**
     * 商品单价
     */
      @ApiModelProperty("商品单价")
    @TableField("product_price")
    private BigDecimal productPrice;

      /**
     * 商品数量
     */
      @ApiModelProperty("商品数量")
    @TableField("product_quantity")
    private Integer productQuantity;

      /**
     * 商品小图
     */
      @ApiModelProperty("商品小图")
    @TableField("product_icon")
    private String productIcon;

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
