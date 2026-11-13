package com.test.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.entity.OrderMaster;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author wyx
 * @since 2026-08-18
 */
public interface OrderMasterMapper extends BaseMapper<OrderMaster> {
    @Update({"<script>",
            "UPDATE order_master",
            "SET order_status = 2",
            "WHERE order_id = #{orderId} AND order_status = 0",
            "<if test='buyerId != null'>",
            "AND buyer_openid = #{buyerId}",
            "</if>",
            "<script>"
    })
    int cancelIfPending(@Param("orderId") String orderId, @Param("buyerId") Integer buyerId);
}
