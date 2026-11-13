package com.test.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.dto.BarDTO;
import com.test.dto.BasicLineDTO;
import com.test.dto.StackedLineDTO;
import com.test.entity.OrderDetail;
import com.test.entity.OrderMaster;
import com.test.exception.ShopException;
import com.test.feign.ProductFeign;
import com.test.mapper.OrderDetailMapper;
import com.test.result.ResponseEnum;
import com.test.service.OrderDetailService;
import com.test.service.OrderMasterService;
import com.test.util.EChartsColorUtil;
import com.test.util.ResultVOUtil;
import com.test.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author wyx
 * @since 2026-08-18
 */
@RestController
@RequestMapping("/seller/order")
public class OrderSellerController {
    @Autowired
    private OrderMasterService orderMasterService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ProductFeign productFeign;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    /**
     * 查询订单
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/list/{page}/{size}")
    public ResultVO list(@PathVariable Integer page, @PathVariable Integer size) {
        Page<OrderMaster> pageModel = new Page<>(page, size);
        Page<OrderMaster> resultPage = this.orderMasterService.page(pageModel);
        PageVO pageVO = new PageVO();
        pageVO.setSize(resultPage.getSize());
        pageVO.setTotal(resultPage.getTotal());
        pageVO.setContent(resultPage.getRecords());
        return ResultVOUtil.success(pageVO);
    }

    /**
     * 取消订单
     * @param orderId
     * @return
     */
    @PutMapping("/cancel/{orderId}")
    public ResultVO cancel(@PathVariable String orderId){
        OrderMaster orderMaster = this.orderMasterService.getById(orderId);
        if(orderMaster == null) throw new ShopException(ResponseEnum.ORDER_NULL.getMsg());
        if(orderMaster.getOrderStatus() == 1) throw new ShopException(ResponseEnum.ORDER_FINISH.getMsg());
        if(orderMaster.getOrderStatus() == 2) throw new ShopException(ResponseEnum.ORDER_CANCEL.getMsg());
        boolean cancelled = this.orderMasterService.cancelIfPending(orderId, null);
        if(!cancelled) throw new ShopException(ResponseEnum.ORDER_CANCEL.getMsg());
        //恢复库存
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
        OrderMaster orderMaster = this.orderMasterService.getById(orderId);
        if(orderMaster == null) throw new ShopException(ResponseEnum.ORDER_NULL.getMsg());
        if(orderMaster.getOrderStatus() == 1) throw new ShopException(ResponseEnum.ORDER_FINISH_ERROR.getMsg());
        if(orderMaster.getOrderStatus() == 2) throw new ShopException(ResponseEnum.ORDER_CANCEL_ERROR.getMsg());
        if(orderMaster.getPayStatus() == 0) throw new ShopException(ResponseEnum.ORDER_PAY_ERROR.getMsg());
        orderMaster.setOrderStatus(1);
        boolean updateById = this.orderMasterService.updateById(orderMaster);
        if(!updateById) return ResultVOUtil.fail(ResponseEnum.ORDER_FINISH_FAIL.getMsg());
        return ResultVOUtil.success(null);
    }

    /**
     * 柱状图
     * @return
     */
    @GetMapping("/barSale")
    public ResultVO barSale(){
        List<BarDTO> barDTOList = this.orderDetailMapper.BarList();
        List<String> namesList = new ArrayList<>();
        List<BarInnerVO> valuesList = new ArrayList<>();
        for(BarDTO barDTO : barDTOList){
            namesList.add(barDTO.getName());
            BarInnerVO barInnerVO = new BarInnerVO();
            barInnerVO.setValue(barDTO.getValue());
            Map<String, String> itemStyle = EChartsColorUtil.createItemStyle(barDTO.getValue());
            barInnerVO.setItemStyle(itemStyle);
            valuesList.add(barInnerVO);
        }
        BarVO barVO = new BarVO();
        barVO.setNames(namesList);
        barVO.setValues(valuesList);
        return ResultVOUtil.success(barVO);
    }

    /**
     * 基础折线图
     * @return
     */
    @GetMapping("/basicLineSale")
    public ResultVO basicLineSale(){
        List<BasicLineDTO> basicLineDTOList = this.orderDetailMapper.BasicLineList();
        List<String> dateList = new ArrayList<>();
        List<Integer> valueList = new ArrayList<>();
        for(BasicLineDTO basicLineDTO : basicLineDTOList){
            dateList.add(basicLineDTO.getDate());
            valueList.add(basicLineDTO.getValue());
        }
        Map<String,List> map = new HashMap<>();
        map.put("names",dateList);
        map.put("values",valueList);
        return ResultVOUtil.success(map);
    }

    /**
     * 折线图堆叠
     * @return
     */
    @GetMapping("/stackedLineSale")
    public ResultVO stackedLineSale(){
        StackedLineVO stackedLineVO = new StackedLineVO();
        List<String> nameList = this.orderDetailMapper.getProductNames();
        stackedLineVO.setNames(nameList);
        List<String> dateList = this.orderDetailMapper.getStackedDate();
        stackedLineVO.setDates(dateList);
        List<StackedLineInnerVO> dataList = new ArrayList<>();
        for (String name : nameList) {
            List<Integer> stackedData = this.orderDetailMapper.getStackedData(name);
            StackedLineInnerVO stackedLineInnerVO = new StackedLineInnerVO();
            stackedLineInnerVO.setName(name);
            stackedLineInnerVO.setData(stackedData);
            dataList.add(stackedLineInnerVO);
        }
        stackedLineVO.setDatas(dataList);
        return ResultVOUtil.success(stackedLineVO);
    }
}
