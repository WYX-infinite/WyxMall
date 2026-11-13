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

class OrderSellerControllerTest {

    private OrderMasterService orderMasterService;
    private OrderDetailService orderDetailService;
    private ProductFeign productFeign;
    private OrderSellerController controller;

    @BeforeEach
    void setUp() {
        orderMasterService = mock(OrderMasterService.class);
        orderDetailService = mock(OrderDetailService.class);
        productFeign = mock(ProductFeign.class);
        controller = new OrderSellerController();

        ReflectionTestUtils.setField(controller, "orderMasterService", orderMasterService);
        ReflectionTestUtils.setField(controller, "orderDetailService", orderDetailService);
        ReflectionTestUtils.setField(controller, "productFeign", productFeign);
    }

    /**
     * 卖家取消成功后，恢复该订单对应的库存。
     */
    @Test
    void cancelRestoresStockAfterConditionalUpdateSucceeds() {
        OrderMaster order = new OrderMaster();
        order.setOrderId("order-1");
        order.setOrderStatus(0);

        OrderDetail detail = new OrderDetail();
        detail.setOrderId("order-1");
        detail.setProductId(79);
        detail.setProductQuantity(2);

        when(orderMasterService.getById("order-1")).thenReturn(order);
        when(orderMasterService.cancelIfPending("order-1", null)).thenReturn(true);
        when(orderDetailService.list(org.mockito.ArgumentMatchers.<Wrapper<OrderDetail>>any()))
                .thenReturn(Collections.singletonList(detail));

        controller.cancel("order-1");
        verify(orderMasterService).cancelIfPending("order-1", null);
        verify(productFeign).subStockById(79, -2);
        // 重点确认卖家入口不再调用危险的 update(entity, null)
        verify(orderMasterService, never()).update(any(OrderMaster.class), isNull());
        verify(orderMasterService, never()).updateById(any(OrderMaster.class));
    }

    /**
     * 模拟另一个请求已经抢先取消订单。
     * 当前请求不能再次恢复库存。
     */
    @Test
    void cancelDoesNotRestoreStockWhenConditionalUpdateFails() {
        OrderMaster order = new OrderMaster();
        order.setOrderId("order-1");
        order.setOrderStatus(0);

        when(orderMasterService.getById("order-1")).thenReturn(order);
        when(orderMasterService.cancelIfPending("order-1", null)).thenReturn(false);

        assertThrows(ShopException.class, () -> controller.cancel("order-1"));
        verify(orderMasterService).cancelIfPending("order-1", null);
        verify(orderDetailService, never()).list(org.mockito.ArgumentMatchers.<Wrapper<OrderDetail>>any());
        verify(productFeign, never()).subStockById(anyInt(), anyInt());
    }

    /**
     * 已经取消的订单，不应再次执行条件更新。
     */
    @Test
    void alreadyCancelledOrderDoesNotTryToRestoreStock() {
        OrderMaster order = new OrderMaster();
        order.setOrderId("order-1");
        order.setOrderStatus(2);

        when(orderMasterService.getById("order-1")).thenReturn(order);

        assertThrows(ShopException.class, () -> controller.cancel("order-1"));

        verify(orderMasterService, never()).cancelIfPending("order-1", null);
        verify(productFeign, never()).subStockById(anyInt(), anyInt());
    }
}