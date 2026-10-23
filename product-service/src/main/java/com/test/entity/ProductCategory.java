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
import java.time.LocalDateTime;

/**
 * <p>
 * 类目表
 * </p>
 *
 * @author wyx
 * @since 2026-08-15
 */
@Getter
@Setter
@ToString
@TableName("product_category")
@ApiModel(value = "ProductCategory对象", description = "类目表")
public class ProductCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "category_id", type = IdType.AUTO)
    private Integer categoryId;

      /**
     * 类目名称
     */
      @ApiModelProperty("类目名称")
    @TableField("category_name")
    private String categoryName;

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
}
