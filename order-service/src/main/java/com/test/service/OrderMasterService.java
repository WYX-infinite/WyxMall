package com.test.service;

import com.test.entity.OrderMaster;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author wyx
 * @since 2026-08-18
 */
public interface OrderMasterService extends IService<OrderMaster> {
    boolean cancelIfPending(String orderId,Integer buyerId);
}
