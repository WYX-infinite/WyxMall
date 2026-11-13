package com.test.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.test.entity.OrderDetail;
import com.test.entity.OrderMaster;
import com.test.exception.ShopException;
import com.test.feign.ProductFeign;
import com.test.service.OrderDetailService;
import com.test.service.OrderMasterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderBuyerControllerTest {
    private OrderMasterService orderMasterService;
    private OrderDetailService orderDetailService;
    private ProductFeign productFeign;
    private OrderBuyerController controller;

    @BeforeEach
    void setUp() {
        orderMasterService = mock(OrderMasterService.class);
        orderDetailService = mock(OrderDetailService.class);
        productFeign = mock(ProductFeign.class);
        controller = new OrderBuyerController();
        ReflectionTestUtils.setField(controller, "orderMasterService", orderMasterService);
        ReflectionTestUtils.setField(controller, "orderDetailService", orderDetailService);
        ReflectionTestUtils.setField(controller, "productFeign", productFeign);
    }

    /**
     * 条件更新成功后，才能恢复库存。
     */
    @Test
    void cancelRestoresStockAfterConditionalUpdateSucceeds() {
        OrderMaster order = new OrderMaster();
        order.setOrderId("order-1");
        order.setBuyerOpenid(27);
        order.setOrderStatus(0);

        OrderDetail detail = new OrderDetail();
        detail.setOrderId("order-1");
        detail.setProductId(79);
        detail.setProductQuantity(2);

        when(orderMasterService.getOne(any())).thenReturn(order);
        when(orderMasterService.cancelIfPending("order-1", 27)).thenReturn(true);
        when(orderDetailService.list(org.mockito.ArgumentMatchers.<Wrapper<OrderDetail>>any()))
                .thenReturn(Collections.singletonList(detail));
        controller.cancel(27, "order-1");
        verify(orderMasterService).cancelIfPending("order-1", 27);
        verify(productFeign).subStockById(79, -2);

        // 确认旧的更新方式已经不再被使用
        verify(orderMasterService, never()).updateById(any(OrderMaster.class));
        verify(orderMasterService, never()).update(any(OrderMaster.class), isNull());
    }

    /**
     * 模拟两个并发请求中，本请求没有取得取消资格。
     * 此时绝对不能恢复库存。
     */
    @Test
    void cancelDoesNotRestoreStockWhenConditionalUpdateFails() {
        OrderMaster order = new OrderMaster();
        order.setOrderId("order-1");
        order.setBuyerOpenid(27);
        order.setOrderStatus(0);

        when(orderMasterService.getOne(any())).thenReturn(order);
        when(orderMasterService.cancelIfPending("order-1", 27)).thenReturn(false);

        assertThrows(ShopException.class, () -> controller.cancel(27, "order-1"));

        verify(orderMasterService).cancelIfPending("order-1", 27);

        verify(orderDetailService, never()).list(org.mockito.ArgumentMatchers.<Wrapper<OrderDetail>>any());

        verify(productFeign, never()).subStockById(anyInt(), anyInt());
    }

    /**
     * 查询时已经发现订单被取消，不应再执行条件更新。
     */
    @Test
    void alreadyCancelledOrderDoesNotTryToRestoreStock() {
        OrderMaster order = new OrderMaster();
        order.setOrderId("order-1");
        order.setBuyerOpenid(27);
        order.setOrderStatus(2);

        when(orderMasterService.getOne(any())).thenReturn(order);

        assertThrows(ShopException.class, () -> controller.cancel(27, "order-1"));

        verify(orderMasterService, never()).cancelIfPending("order-1", 27);
        verify(productFeign, never()).subStockById(anyInt(), anyInt());
    }
}