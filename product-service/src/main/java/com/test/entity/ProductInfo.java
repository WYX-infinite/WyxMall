package com.test.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 商品表
 * </p>
 *
 * @author wyx
 * @since 2026-08-15
 */
@Getter
@Setter
@ToString
@TableName("product_info")
@ApiModel(value = "ProductInfo对象", description = "商品表")
public class ProductInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "product_id", type = IdType.AUTO)
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
     * 库存
     */
      @ApiModelProperty("库存")
    @TableField("product_stock")
    private Integer productStock;

      /**
     * 描述
     */
      @ApiModelProperty("描述")
    @TableField("product_description")
    private String productDescription;

      /**
     * 小图
     */
      @ApiModelProperty("小图")
    @TableField("product_icon")
    private String productIcon;

      /**
     * 类目编号
     */
      @ApiModelProperty("类目编号")
    @TableField("category_type")
    private Integer categoryType;

      /**
     * 创建时间
     */
      @ApiModelProperty("创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

      /**
     * 修改时间
     */
      @ApiModelProperty("修改时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

      /**
     * 商品状态，1正常0下架
     */
      @TableField("product_status")
    @ApiModelProperty("商品状态，1正常0下架")
    private Integer productStatus;
}
