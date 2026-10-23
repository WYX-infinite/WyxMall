package com.test.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.dto.BarDTO;
import com.test.dto.BasicLineDTO;
import com.test.dto.StackedLineDTO;
import com.test.entity.OrderDetail;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 订单详情表 Mapper 接口
 * </p>
 *
 * @author wyx
 * @since 2026-08-18
 */
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
    @Select({"select product_name name,sum(product_quantity) value from order_detail group by product_name"})
    public List<BarDTO> BarList();

    @Select({"select date_format(create_time,'%Y-%m-%d') date,sum(product_quantity) value from order_detail group by date_format(create_time,'%Y-%m-%d');"})
    public List<BasicLineDTO> BasicLineList();

    @Select({"select product_name from product_info"})
    public List<String> getProductNames();

    @Select({"select distinct date_format(create_time,'%Y-%m-%d') from order_detail"})
    public List<String> getStackedDate();

    @Select({"select(\n" +
            "    select COALESCE(sum(product_quantity),0) from order_detail\n" +
            "    where pi.product_id = order_detail.product_id and DATE_FORMAT(order_detail.create_time, '%Y-%m-%d') = mm.dd\n" +
            "    ) as count\n" +
            "from product_info pi, (select distinct date_format(create_time,'%Y-%m-%d') as dd from order_detail) mm\n" +
            "where product_name = #{productName} order by mm.dd;"})
    public List<Integer> getStackedData(String productName);
}
