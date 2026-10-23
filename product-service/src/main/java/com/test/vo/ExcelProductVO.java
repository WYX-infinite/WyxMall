package com.test.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExcelProductVO {
    @ExcelProperty("商品编号")
    private Integer productId;
    @ExcelProperty("商品名称")
    private String productName;
    @ExcelProperty("商品价格")
    private BigDecimal productPrice;
    @ExcelProperty("商品库存")
    private Integer productStock;
    @ExcelProperty("商品描述")
    private String productDescription;
    @ExcelProperty("商品图标")
    private String productIcon;
    @ExcelProperty("商品分类")
    private Integer categoryType;
    @ExcelProperty("分类名称")
    private String categoryName;
    @ExcelProperty("商品状态")
    private String productStatus;
}
