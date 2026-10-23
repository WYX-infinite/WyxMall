package com.test.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.test.entity.ProductCategory;
import com.test.entity.ProductInfo;
import com.test.mapper.ProductCategoryMapper;
import com.test.mapper.ProductInfoMapper;
import com.test.service.ProductCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.test.vo.CategoryVO;
import com.test.vo.InfoVO;
import com.test.vo.ResultVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 类目表 服务实现类
 * </p>
 *
 * @author wyx
 * @since 2026-08-15
 */
@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {
    @Autowired
    private ProductCategoryMapper productCategoryMapper;
    @Autowired
    private ProductInfoMapper productInfoMapper;

    @Override
    public ResultVO categoryList() {
        List<CategoryVO> categoryVOList = new ArrayList<>();
        List<ProductCategory> categoryList = this.productCategoryMapper.selectList(null);
        for(ProductCategory productCategory :categoryList){
            CategoryVO categoryVO = new CategoryVO();
            BeanUtils.copyProperties(productCategory,categoryVO);
            QueryWrapper<ProductInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("category_type",productCategory.getCategoryId());
            List<ProductInfo> productInfoList = this.productInfoMapper.selectList(queryWrapper);
            List<InfoVO> infoVOList = new ArrayList<>();
            for(ProductInfo productInfo : productInfoList){
                InfoVO infoVO = new InfoVO();
                BeanUtils.copyProperties(productInfo,infoVO);
                infoVO.setQuantity(0);
                infoVOList.add(infoVO);
            }
            categoryVO.setGoods(infoVOList);
            categoryVOList.add(categoryVO);
        }
        ResultVO resultVO = new ResultVO();
        resultVO.setCode(0);
        resultVO.setMsg("成功");
        resultVO.setData(categoryVOList);
        return resultVO;
    }
}
