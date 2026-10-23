package com.test.service;

import com.test.entity.ProductCategory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.test.vo.ResultVO;

/**
 * <p>
 * 类目表 服务类
 * </p>
 *
 * @author wyx
 * @since 2026-08-15
 */
public interface ProductCategoryService extends IService<ProductCategory> {
    public ResultVO categoryList();
}
