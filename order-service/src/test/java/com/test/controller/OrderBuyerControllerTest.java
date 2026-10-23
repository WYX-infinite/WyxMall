package com.test.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.test.entity.OrderDetail;
import com.test.entity.OrderMaster;
import com.test.feign.ProductFeign;
import com.test.service.OrderDetailService;
import com.test.service.OrderMasterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderBuyerControllerTest {
    private OrderMasterService orderMasterService;
    private OrderDetailService orderDetailService;
    private OrderBuyerController controller;

    @BeforeEach
    void setUp() {
        orderMasterService = mock(OrderMasterService.class);
        orderDetailService = mock(OrderDetailService.class);
        controller = new OrderBuyerController();
        ReflectionTestUtils.setField(controller, "orderMasterService", orderMasterService);
        ReflectionTestUtils.setField(controller, "orderDetailService", orderDetailService);
        ReflectionTestUtils.setField(controller, "productFeign", mock(ProductFeign.class));
    }

    @Test
    void cancelUpdatesOnlyTheSelectedOrder() {
        OrderMaster order = new OrderMaster();
        order.setOrderId("order-1");
        order.setBuyerOpenid(27);
        order.setOrderStatus(0);
        when(orderMasterService.getOne(any())).thenReturn(order);
        when(orderMasterService.updateById(order)).thenReturn(true);
        when(orderDetailService.list(org.mockito.ArgumentMatchers.<Wrapper<OrderDetail>>any()))
                .thenReturn(Collections.emptyList());

        controller.cancel(27, "order-1");

        verify(orderMasterService).updateById(order);
        verify(orderMasterService, never()).update(any(OrderMaster.class), isNull());
    }
}
