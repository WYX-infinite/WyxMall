package com.test.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.test.entity.ProductInfo;
import com.test.exception.ShopException;
import com.test.mapper.ProductInfoMapper;
import com.test.result.ResponseEnum;
import com.test.service.ProductInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.test.vo.ExcelProductVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author wyx
 * @since 2026-08-15
 */
@Service
public class ProductInfoServiceImpl extends ServiceImpl<ProductInfoMapper, ProductInfo> implements ProductInfoService {
    @Autowired
    private ProductInfoMapper productInfoMapper;

    @Override
    public BigDecimal findPriceById(Integer id) {
        ProductInfo productInfo = productInfoMapper.selectById(id);
        if (productInfo == null) throw new ShopException(ResponseEnum.PRODUCT_NULL.getMsg());
        return productInfo.getProductPrice();
    }

    @Override
    public Boolean subStockById(Integer id, Integer quantity) {
        if (quantity == null || quantity == 0) {
            return true;
        }
        int affectedRows;
        if (quantity > 0) {
            affectedRows = this.productInfoMapper.decreaseStock(id, quantity);
            if (affectedRows == 0) {
                ProductInfo productInfo = this.productInfoMapper.selectById(id);
                if (productInfo == null) throw new ShopException(ResponseEnum.PRODUCT_NULL.getMsg());
                throw new ShopException(ResponseEnum.PRODUCT_STOCK_EMPTY.getMsg());
            }
        } else {
            affectedRows = this.productInfoMapper.increaseStock(id, -quantity);
            if (affectedRows == 0) throw new ShopException(ResponseEnum.PRODUCT_NULL.getMsg());
        }
        return true;
    }

    @Override
    public List<ProductInfo> excelToProductInfoList(InputStream inputStream) {
        List<ProductInfo> list = new ArrayList<>();
        EasyExcel.read(inputStream)
                .head(ExcelProductVO.class)
                .sheet()
                .registerReadListener(new AnalysisEventListener<ExcelProductVO>() {
                    @Override
                    public void invoke(ExcelProductVO excelData, AnalysisContext analysisContext) {
                        ProductInfo productInfo = new ProductInfo();
                        BeanUtils.copyProperties(excelData, productInfo);
                        if(excelData.getProductStatus().equals("上架")){
                            productInfo.setProductStatus(1);
                        }else{
                            productInfo.setProductStatus(0);
                        }
                        list.add(productInfo);
                    }

                    @Override
                    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                        System.out.println("=========================文件解析完成=========================");
                    }
                }).doRead();
        return list;
    }
}
