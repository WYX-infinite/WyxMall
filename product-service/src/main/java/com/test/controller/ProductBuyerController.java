package com.test.controller;

import com.test.entity.ProductInfo;
import com.test.exception.ShopException;
import com.test.result.ResponseEnum;
import com.test.service.ProductCategoryService;
import com.test.service.ProductInfoService;
import com.test.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * <p>
 * 类目表 前端控制器
 * </p>
 *
 * @author wyx
 * @since 2026-08-15
 */
@RestController
@RequestMapping("/buyer/product")
public class ProductBuyerController {
    @Autowired
    private ProductCategoryService productCategoryService;
    @Autowired
    private ProductInfoService productInfoService;

    @GetMapping("/list")
    public ResultVO list(){
        return this.productCategoryService.categoryList();
    }

    @GetMapping("/findPriceById/{id}")
    public BigDecimal findPriceById(@PathVariable Integer id){
        return this.productInfoService.findPriceById(id);
    }

    @GetMapping("/findById/{id}")
    public ProductInfo findById(@PathVariable Integer id){
        ProductInfo productInfo = this.productInfoService.getById(id);
        if(productInfo == null) throw new ShopException(ResponseEnum.PRODUCT_NULL.getMsg());
        return productInfo;
    }

    @PutMapping("/subStockById/{id}/{quantity}")
    public Boolean subStockById(@PathVariable Integer id, @PathVariable Integer quantity){
        return this.productInfoService.subStockById(id,quantity);
    }

}
