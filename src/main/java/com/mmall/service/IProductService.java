package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVo;


public interface IProductService {

    /**
     * 更新或新增产品
     */
    ServerResponse saveOrUpdateProduct(Product product);

    /**
     * 产品上下架，更新产品的状态
     */
    ServerResponse<String> setSaleStatus(Integer productId,Integer status);

    /**
     *后台获取商品
     */
    ServerResponse<ProductDetailVo> manageProductDetail(Integer productId);

    /**
     * 后台商品列表动态功能
     */
    ServerResponse getProductList(int pageNum,int pageSize);

    /**
     * 后台商品搜索功能
     */
    ServerResponse<PageInfo> searchProduct(String productName,Integer productId,int pageNum,int pageSize);

    /**
     * 用户查询商品
     */
    ServerResponse<ProductDetailVo> getProductDetail(Integer productId);

    /**
     *查询页面信息
     */
    ServerResponse<PageInfo> getProductByKeywordCategory(String keyword,Integer categoryId,int pageNum,int pageSize,String orderBy);
}
