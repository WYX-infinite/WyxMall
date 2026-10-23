package com.test.service;

import com.test.entity.ProductInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 商品表 服务类
 * </p>
 *
 * @author wyx
 * @since 2026-08-15
 */
public interface ProductInfoService extends IService<ProductInfo> {

    BigDecimal findPriceById(Integer id);

    Boolean subStockById(Integer id, Integer quantity);

    public List<ProductInfo> excelToProductInfoList(InputStream inputStream);
}
