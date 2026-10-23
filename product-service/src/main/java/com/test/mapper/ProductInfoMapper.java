package com.test.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.entity.ProductInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 商品表 Mapper 接口
 * </p>
 *
 * @author wyx
 * @since 2026-08-15
 */
public interface ProductInfoMapper extends BaseMapper<ProductInfo> {

    @Update("UPDATE product_info SET product_stock = product_stock - #{quantity} " +
            "WHERE product_id = #{id} AND product_stock >= #{quantity}")
    int decreaseStock(@Param("id") Integer id, @Param("quantity") Integer quantity);

    @Update("UPDATE product_info SET product_stock = product_stock + #{quantity} " +
            "WHERE product_id = #{id}")
    int increaseStock(@Param("id") Integer id, @Param("quantity") Integer quantity);

}
