package com.test.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.entity.ProductCategory;
import com.test.entity.ProductInfo;
import com.test.exception.ShopException;
import com.test.form.ProductForm;
import com.test.form.ProductInnerForm;
import com.test.handler.CustomCellWriteHandler;
import com.test.result.ResponseEnum;
import com.test.service.ProductCategoryService;
import com.test.service.ProductInfoService;
import com.test.util.ResultVOUtil;
import com.test.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wyx
 * @since 2026-08-15
 */
@RestController
@RequestMapping("/seller/product")
public class ProductSellerController {
    @Autowired
    private ProductCategoryService productCategoryService;
    @Autowired
    private ProductInfoService productInfoService;

    /**
     * 查询所有商品分类
     * @return
     */
    @GetMapping("/findAllProductCategory")
    public ResultVO findAllProductCategory(){
        List<ProductCategory> list = this.productCategoryService.list();
        List<SellerProductCategoryVO> sellerProductCategoryVOList = new ArrayList<>();
        for (ProductCategory productCategory : list) {
            SellerProductCategoryVO sellerProductCategoryVO = new SellerProductCategoryVO();
            sellerProductCategoryVO.setName(productCategory.getCategoryName());
            sellerProductCategoryVO.setType(productCategory.getCategoryType());
            sellerProductCategoryVOList.add(sellerProductCategoryVO);
        }
        Map map = new HashMap();
        map.put("content", sellerProductCategoryVOList);
        return ResultVOUtil.success(map);
    }

    /**
     * 添加商品
     * @param productInfo
     * @return
     */
    @PostMapping("/add")
    public ResultVO add(@RequestBody ProductInfo productInfo){
        Integer categoryType = productInfo.getCategoryType();
        QueryWrapper<ProductCategory> productCategoryQueryWrapper = new QueryWrapper<>();
        productCategoryQueryWrapper.eq("category_type", categoryType);
        ProductCategory category = this.productCategoryService.getOne(productCategoryQueryWrapper);
        if(category == null) throw new ShopException(ResponseEnum.PRODUCT_CATEGORY_ERROR.getMsg());
        boolean save = this.productInfoService.save(productInfo);
        if(!save) return ResultVOUtil.fail(ResponseEnum.PRODUCT_ADD_ERROR.getMsg());
        return ResultVOUtil.success(null);
    }

    /**
     * 查询商品
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/list/{page}/{size}")
    public ResultVO list(@PathVariable Integer page, @PathVariable Integer size){
        Page<ProductInfo> pageModel = new Page<>(page, size);
        Page<ProductInfo> resultPage = this.productInfoService.page(pageModel, null);
        List<ProductInfo> records = resultPage.getRecords();
        List<SellerProductInfoVO> list = new ArrayList<>();
        for (ProductInfo productInfo : records) {
            SellerProductInfoVO sellerProductInfoVO = new SellerProductInfoVO();
            if(productInfo.getProductStatus() == 1){
                sellerProductInfoVO.setStatus(true);
            }else {
                sellerProductInfoVO.setStatus(false);
            }
            QueryWrapper<ProductCategory> productCategoryQueryWrapper = new QueryWrapper<>();
            productCategoryQueryWrapper.eq("category_type", productInfo.getCategoryType());
            ProductCategory category = this.productCategoryService.getOne(productCategoryQueryWrapper);
            sellerProductInfoVO.setCategoryName(category.getCategoryName());
            BeanUtils.copyProperties(productInfo, sellerProductInfoVO);
            list.add(sellerProductInfoVO);
        }
        PageVO pageVO = new PageVO();
        pageVO.setContent(list);
        pageVO.setSize(resultPage.getSize());
        pageVO.setTotal(resultPage.getTotal());
        return ResultVOUtil.success(pageVO);
    }

    /**
     * 商品模糊查询
     * @param keyword
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/like/{keyword}/{page}/{size}")
    public ResultVO like(@PathVariable String keyword, @PathVariable Integer page, @PathVariable Integer size){
        Page<ProductInfo> pageModel = new Page<>(page, size);
        QueryWrapper<ProductInfo> productInfoQueryWrapper = new QueryWrapper<>();
        productInfoQueryWrapper.like("product_name", keyword);
        Page<ProductInfo> resultPage = this.productInfoService.page(pageModel, productInfoQueryWrapper);
        List<ProductInfo> records = resultPage.getRecords();
        List<SellerProductInfoVO> list = new ArrayList<>();
        for (ProductInfo productInfo : records) {
            SellerProductInfoVO sellerProductInfoVO = new SellerProductInfoVO();
            if(productInfo.getProductStatus() == 1){
                sellerProductInfoVO.setStatus(true);
            }else {
                sellerProductInfoVO.setStatus(false);
            }
            QueryWrapper<ProductCategory> productCategoryQueryWrapper = new QueryWrapper<>();
            productCategoryQueryWrapper.eq("category_type", productInfo.getCategoryType());
            ProductCategory category = this.productCategoryService.getOne(productCategoryQueryWrapper);
            sellerProductInfoVO.setCategoryName(category.getCategoryName());
            BeanUtils.copyProperties(productInfo, sellerProductInfoVO);
            list.add(sellerProductInfoVO);
        }
        PageVO pageVO = new PageVO();
        pageVO.setContent(list);
        pageVO.setSize(pageModel.getSize());
        pageVO.setTotal(pageModel.getTotal());
        return ResultVOUtil.success(pageVO);
    }

    /**
     * 通过分类查询商品
     * @param categoryType
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/findByCategory/{categoryType}/{page}/{size}")
    public ResultVO findByCategory(@PathVariable Integer categoryType, @PathVariable Integer page, @PathVariable Integer size){
        Page<ProductInfo> pageModel = new Page<>(page, size);
        QueryWrapper<ProductInfo> productInfoQueryWrapper = new QueryWrapper<>();
        productInfoQueryWrapper.eq("category_type", categoryType);
        Page<ProductInfo> resultPage = this.productInfoService.page(pageModel, productInfoQueryWrapper);
        List<ProductInfo> records = resultPage.getRecords();
        List<SellerProductInfoVO> list = new ArrayList<>();
        for (ProductInfo productInfo : records) {
            SellerProductInfoVO sellerProductInfoVO = new SellerProductInfoVO();
            if(productInfo.getProductStatus() == 1){
                sellerProductInfoVO.setStatus(true);
            }else {
                sellerProductInfoVO.setStatus(false);
            }
            QueryWrapper<ProductCategory> productCategoryQueryWrapper = new QueryWrapper<>();
            productCategoryQueryWrapper.eq("category_type", productInfo.getCategoryType());
            ProductCategory category = this.productCategoryService.getOne(productCategoryQueryWrapper);
            sellerProductInfoVO.setCategoryName(category.getCategoryName());
            BeanUtils.copyProperties(productInfo, sellerProductInfoVO);
            list.add(sellerProductInfoVO);
        }
        PageVO pageVO = new PageVO();
        pageVO.setContent(list);
        pageVO.setSize(pageModel.getSize());
        pageVO.setTotal(pageModel.getTotal());
        return ResultVOUtil.success(pageVO);
    }

    /**
     * 通过 ID 查询商品
     * @param id
     * @return
     */
    @GetMapping("/findById/{id}")
    public ResultVO findById(@PathVariable Integer id){
        ProductInfo productInfo = this.productInfoService.getById(id);
        if(productInfo == null) throw new ShopException(ResponseEnum.PRODUCT_NULL.getMsg());
        SellerProductFindVO sellerProductFindVO = new SellerProductFindVO();
        sellerProductFindVO.setId(productInfo.getProductId());
        sellerProductFindVO.setName(productInfo.getProductName());
        sellerProductFindVO.setPrice(productInfo.getProductPrice());
        sellerProductFindVO.setStock(productInfo.getProductStock());
        sellerProductFindVO.setDescription(productInfo.getProductDescription());
        sellerProductFindVO.setIcon(productInfo.getProductIcon());
        if(productInfo.getProductStatus() == 1){
            sellerProductFindVO.setStatus(true);
        }else {
            sellerProductFindVO.setStatus(false);
        }
        Map map = new HashMap();
        map.put("categoryType", productInfo.getCategoryType());
        sellerProductFindVO.setCategory(map);
        return ResultVOUtil.success(sellerProductFindVO);
    }

    /**
     * 通过 ID 删除商品
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public ResultVO delete(@PathVariable Integer id){
        ProductInfo productInfo = this.productInfoService.getById(id);
        if(productInfo == null) throw new ShopException(ResponseEnum.PRODUCT_NULL.getMsg());
        boolean remove = this.productInfoService.removeById(id);
        if(!remove) return ResultVOUtil.fail(ResponseEnum.PRODUCT_DELETE_ERROR.getMsg());
        return ResultVOUtil.success(null);
    }

    /**
     * 修改商品状态
     * @param id
     * @param status
     * @return
     */
    @PutMapping("/updateStatus/{id}/{status}")
    public ResultVO updateStatus(@PathVariable Integer id, @PathVariable Boolean status){
        ProductInfo productInfo = this.productInfoService.getById(id);
        if(productInfo == null) throw new ShopException(ResponseEnum.PRODUCT_NULL.getMsg());
        if(status && productInfo.getProductStatus() == 1){
            throw new ShopException(ResponseEnum.PRODUCT_STATUS_TRUE_CHANGE_ERROR.getMsg());
        }
        if(!status && productInfo.getProductStatus() == 0){
            throw new ShopException(ResponseEnum.PRODUCT_STATUS_FALSE_CHANGE_ERROR.getMsg());
        }
        if(status){
            productInfo.setProductStatus(1);
        } else{
            productInfo.setProductStatus(0);
        }
        boolean updateById = this.productInfoService.updateById(productInfo);
        if(!updateById) return ResultVOUtil.fail(ResponseEnum.PRODUCT_STATUS_ERROR.getMsg());
        return ResultVOUtil.success(status);
    }

    /**
     * 修改商品
     * @param productForm
     * @return
     */
    @PutMapping("/update")
    public ResultVO update(@RequestBody ProductForm productForm){
        ProductInfo productInfo = this.productInfoService.getById(productForm.getId());
        if(productInfo == null) throw new ShopException(ResponseEnum.PRODUCT_NULL.getMsg());
        if(productForm.getStatus()){
            productInfo.setProductStatus(1);
        }else{
            productInfo.setProductStatus(0);
        }
        productInfo.setProductName(productForm.getName());
        productInfo.setProductPrice(productForm.getPrice());
        productInfo.setProductStock(productForm.getStock());
        productInfo.setProductDescription(productForm.getDescription());
        productInfo.setProductIcon(productForm.getIcon());
        productInfo.setCategoryType(productForm.getCategory().getCategoryType());
        boolean updateById = this.productInfoService.updateById(productInfo);
        if(!updateById) return ResultVOUtil.fail(ResponseEnum.PRODUCT_UPDATE_ERROR.getMsg());
        return ResultVOUtil.success(null);
    }

    /**
     * 导出excel
     * @param response
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response){
        try{
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode("商品信息", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            //把数据库中的数据转成excel格式的数据
            List<ProductInfo> productInfoList = this.productInfoService.list();
            List<ExcelProductVO> excelProductVOList = new ArrayList<>();
            for (ProductInfo productInfo : productInfoList) {
                ExcelProductVO excelProductVO = new ExcelProductVO();
                if(productInfo.getProductStatus() == 1){
                    excelProductVO.setProductStatus("上架");
                }else {
                    excelProductVO.setProductStatus("下架");
                }
                BeanUtils.copyProperties(productInfo, excelProductVO);
                QueryWrapper<ProductCategory> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("category_type", productInfo.getCategoryType());
                ProductCategory category = this.productCategoryService.getOne(queryWrapper);
                excelProductVO.setCategoryName(category.getCategoryName());
                excelProductVOList.add(excelProductVO);
            }
            EasyExcel.write(response.getOutputStream(),ExcelProductVO.class)
                    .registerWriteHandler(new CustomCellWriteHandler())
                    .sheet("商品信息")
                    .doWrite(excelProductVOList);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 导入excel
     * @param file
     * @return
     */
    @PostMapping("/import")
    public ResultVO importData(@RequestParam MultipartFile file){
        List<ProductInfo> productInfoList = null;
        try {
            productInfoList = this.productInfoService.excelToProductInfoList(file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(productInfoList == null) return ResultVOUtil.fail("导入Excel失败！");
        boolean res = this.productInfoService.saveBatch(productInfoList);
        if(!res) return ResultVOUtil.fail("导入Excel失败！");
        return ResultVOUtil.success(null);
    }
}
