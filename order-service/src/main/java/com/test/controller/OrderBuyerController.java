package com.test.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.entity.OrderDetail;
import com.test.entity.OrderMaster;
import com.test.entity.ProductInfo;
import com.test.event.NewOrderEvent;
import com.test.exception.ShopException;
import com.test.feign.ProductFeign;
import com.test.form.BuyerOrderForm;
import com.test.form.BuyerOrderInnerForm;
import com.test.result.ResponseEnum;
import com.test.service.OrderDetailService;
import com.test.service.OrderMasterService;
import com.test.util.ResultVOUtil;
import com.test.vo.BuyerOrderDetailVO;
import com.test.vo.BuyerOrderMasterVO;
import com.test.vo.ResultVO;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <p>
 * 订单详情表 前端控制器
 * </p>
 *
 * @author wyx
 * @since 2026-08-18
 */
@RestController
@RequestMapping("/buyer/order")
public class OrderBuyerController {
    @Resource
    private ProductFeign productFeign;
    @Autowired
    private OrderMasterService orderMasterService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 创建订单
     * @param buyerOrderForm
     * @return
     */
    @PostMapping("/create")
    public ResultVO create(@RequestBody BuyerOrderForm buyerOrderForm) {
        //添加主表order_master
        List<BuyerOrderInnerForm> items = buyerOrderForm.getItems();
        BigDecimal orderAmount = new BigDecimal(0);
        for (BuyerOrderInnerForm item : items) {
            //根据id查询价格
            Integer productId = item.getProductId();
            Integer productQuantity = item.getProductQuantity();
            BigDecimal price = productFeign.findPriceById(productId);
            BigDecimal multiply = price.multiply(new BigDecimal(productQuantity));
            orderAmount = orderAmount.add(multiply);
        }
        OrderMaster orderMaster = new OrderMaster();
        orderMaster.setBuyerName(buyerOrderForm.getName());
        orderMaster.setBuyerPhone(buyerOrderForm.getPhone());
        orderMaster.setBuyerAddress(buyerOrderForm.getAddress());
        orderMaster.setBuyerOpenid(buyerOrderForm.getId());
        orderMaster.setOrderStatus(0);
        orderMaster.setPayStatus(0);
        orderMaster.setOrderAmount(orderAmount);
        boolean save = orderMasterService.save(orderMaster);
        if (!save) throw new ShopException(ResponseEnum.ORDER_CREATE_ERROR.getMsg());
        //添加从表order_detail
        for (BuyerOrderInnerForm item : items) {
            Integer productId = item.getProductId();
            Integer productQuantity = item.getProductQuantity();
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderMaster.getOrderId());
            orderDetail.setProductId(productId);
            orderDetail.setProductQuantity(productQuantity);
            ProductInfo productInfo = this.productFeign.findById(productId);
            BeanUtils.copyProperties(productInfo, orderDetail);
            this.orderDetailService.save(orderDetail);
            //减库存
            this.productFeign.subStockById(productId, productQuantity);
        }
        // 结构化事件既供业务提醒，也供本地链路测试精确关联。
        NewOrderEvent event = new NewOrderEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setOrderId(orderMaster.getOrderId());
        event.setTestRunId(buyerOrderForm.getTestRunId());
        event.setSentAt(System.currentTimeMillis());
        try {
            this.rocketMQTemplate.convertAndSend("myTopic", objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("新订单事件序列化失败", e);
        }
        Map map = new HashMap();
        map.put("orderId", orderMaster.getOrderId());
        map.put("eventId", event.getEventId());
        map.put("sentAt", event.getSentAt());
        map.put("testRunId", event.getTestRunId());
        return ResultVOUtil.success(map);
    }

    /**
     * 分页查询订单列表
     * @param buyerId
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/list/{buyerId}/{page}/{pageSize}")
    public ResultVO list(@PathVariable Integer buyerId, @PathVariable Integer page, @PathVariable Integer pageSize) {
        Page<OrderMaster> orderMasterPage = new Page<>(page, pageSize);
        QueryWrapper<OrderMaster> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("buyer_openid", buyerId);
        Page<OrderMaster> resultPage = this.orderMasterService.page(orderMasterPage, queryWrapper);
        return ResultVOUtil.success(resultPage.getRecords());
    }

    /**
     * 查询订单详情
     * @param buyerId
     * @param orderId
     * @return
     */
    @GetMapping("/detail/{buyerId}/{orderId}")
    public ResultVO detail(@PathVariable Integer buyerId, @PathVariable String orderId) {
        QueryWrapper<OrderMaster> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId).eq("buyer_openid", buyerId);
        OrderMaster orderMaster = this.orderMasterService.getOne(queryWrapper);
        if(orderMaster == null) throw new ShopException(ResponseEnum.ORDER_NULL.getMsg());
        BuyerOrderMasterVO buyerOrderMasterVO = new BuyerOrderMasterVO();
        BeanUtils.copyProperties(orderMaster, buyerOrderMasterVO);
        List<OrderDetail> orderDetailList = this.orderDetailService.list(new QueryWrapper<OrderDetail>().eq("order_id", orderId));
        List<BuyerOrderDetailVO> buyerOrderDetailVOList = new ArrayList<>();
        for(OrderDetail orderDetail : orderDetailList){
            BuyerOrderDetailVO buyerOrderDetailVO = new BuyerOrderDetailVO();
            BeanUtils.copyProperties(orderDetail, buyerOrderDetailVO);
            buyerOrderDetailVOList.add(buyerOrderDetailVO);
        }
        buyerOrderMasterVO.setOrderDetailList(buyerOrderDetailVOList);
        return ResultVOUtil.success(buyerOrderMasterVO);
    }

    /**
     * 取消订单
     * @param buyerId
     * @param orderId
     * @return
     */
    @PutMapping("/cancel/{buyerId}/{orderId}")
    public ResultVO cancel(@PathVariable Integer buyerId, @PathVariable String orderId){
        QueryWrapper<OrderMaster> orderMasterQueryWrapper = new QueryWrapper<>();
        orderMasterQueryWrapper.eq("order_id", orderId).eq("buyer_openid", buyerId);
        OrderMaster orderMaster = this.orderMasterService.getOne(orderMasterQueryWrapper);
        if(orderMaster == null) throw new ShopException(ResponseEnum.ORDER_NULL.getMsg());
        if(orderMaster.getOrderStatus() == 1) throw new ShopException(ResponseEnum.ORDER_FINISH.getMsg());
        if(orderMaster.getOrderStatus() == 2) throw new ShopException(ResponseEnum.ORDER_CANCEL.getMsg());
        boolean cancelled = this.orderMasterService.cancelIfPending(orderId, buyerId);
        if(!cancelled) throw new ShopException(ResponseEnum.ORDER_CANCEL.getMsg());
        //只有取得取消资格的请求才能恢复库存
        List<OrderDetail> orderDetailList = this.orderDetailService.list(new QueryWrapper<OrderDetail>().eq("order_id", orderId));
        for(OrderDetail orderDetail : orderDetailList){
            Integer productId = orderDetail.getProductId();
            Integer productQuantity = orderDetail.getProductQuantity();
            this.productFeign.subStockById(productId, -productQuantity);
        }
        return ResultVOUtil.success(null);
    }

    /**
     * 完结订单
     * @param orderId
     * @return
     */
    @PutMapping("/finish/{orderId}")
    public ResultVO finish(@PathVariable String orderId){
        QueryWrapper<OrderMaster> orderMasterQueryWrapper = new QueryWrapper<>();
        orderMasterQueryWrapper.eq("order_id", orderId);
        OrderMaster orderMaster = this.orderMasterService.getOne(orderMasterQueryWrapper);
        if(orderMaster == null) throw new ShopException(ResponseEnum.ORDER_NULL.getMsg());
        if(orderMaster.getOrderStatus() == 1) throw new ShopException(ResponseEnum.ORDER_FINISH_ERROR.getMsg());
        if(orderMaster.getOrderStatus() == 2) throw new ShopException(ResponseEnum.ORDER_CANCEL_ERROR.getMsg());
        orderMaster.setOrderStatus(1);
        boolean updateById = this.orderMasterService.updateById(orderMaster);
        if(!updateById) return ResultVOUtil.fail(ResponseEnum.ORDER_FINISH_FAIL.getMsg());
        return ResultVOUtil.success(null);
    }

    /**
     * 支付订单
     * @param buyerId
     * @param orderId
     * @return
     */
    @PutMapping("/pay/{buyerId}/{orderId}")
    public ResultVO pay(@PathVariable Integer buyerId, @PathVariable String orderId){
        QueryWrapper<OrderMaster> orderMasterQueryWrapper = new QueryWrapper<>();
        orderMasterQueryWrapper.eq("order_id", orderId).eq("buyer_openid", buyerId);
        OrderMaster orderMaster = this.orderMasterService.getOne(orderMasterQueryWrapper);
        if(orderMaster == null) throw new ShopException(ResponseEnum.ORDER_NULL.getMsg());
        if(orderMaster.getOrderStatus() == 1) throw new ShopException(ResponseEnum.ORDER_FINISH_PAY_ERROR.getMsg());
        if(orderMaster.getOrderStatus() == 2) throw new ShopException(ResponseEnum.ORDER_CANCEL_PAY_ERROR.getMsg());
        if(orderMaster.getPayStatus() == 1) throw new ShopException(ResponseEnum.ORDER_PAY_ERROR.getMsg());
        orderMaster.setPayStatus(1);
        boolean updateById = this.orderMasterService.updateById(orderMaster);
        if(!updateById) return ResultVOUtil.fail(ResponseEnum.ORDER_PAY_FAIL.getMsg());
        return ResultVOUtil.success(null);
    }
}
