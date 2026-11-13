package com.test.service.impl;

import com.test.entity.OrderMaster;
import com.test.mapper.OrderMasterMapper;
import com.test.service.OrderMasterService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author wyx
 * @since 2026-08-18
 */
@Service
public class OrderMasterServiceImpl extends ServiceImpl<OrderMasterMapper, OrderMaster> implements OrderMasterService {
    @Override
    public boolean cancelIfPending(String orderId, Integer buyerId) {
        return this.baseMapper.cancelIfPending(orderId,buyerId) == 1;
    }
}
