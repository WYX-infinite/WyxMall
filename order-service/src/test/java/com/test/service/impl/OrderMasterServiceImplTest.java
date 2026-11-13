package com.test.service.impl;

import com.test.mapper.OrderMasterMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderMasterServiceImplTest {

    private OrderMasterMapper mapper;
    private OrderMasterServiceImpl service;

    @BeforeEach
    void setUp() {
        mapper = mock(OrderMasterMapper.class);
        service = new OrderMasterServiceImpl();

        // baseMapper 定义在 MyBatis-Plus 的 ServiceImpl 父类中
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
    }

    /**
     * 影响一行，说明本请求取得了取消资格。
     */
    @Test
    void cancelIfPendingReturnsTrueWhenOneRowIsUpdated() {
        when(mapper.cancelIfPending("order-1", 27)).thenReturn(1);

        boolean result = service.cancelIfPending("order-1", 27);

        assertTrue(result);
        verify(mapper).cancelIfPending("order-1", 27);
    }

    /**
     * 影响零行，通常表示订单已经被其他请求取消。
     */
    @Test
    void cancelIfPendingReturnsFalseWhenNoRowIsUpdated() {
        when(mapper.cancelIfPending("order-1", 27)).thenReturn(0);

        boolean result = service.cancelIfPending("order-1", 27);

        assertFalse(result);
        verify(mapper).cancelIfPending("order-1", 27);
    }

    /**
     * 卖家取消时不限制 buyerId。
     */
    @Test
    void sellerCancellationPassesNullBuyerId() {
        when(mapper.cancelIfPending("order-1", null)).thenReturn(1);

        boolean result = service.cancelIfPending("order-1", null);

        assertTrue(result);
        verify(mapper).cancelIfPending("order-1", null);
    }
}
