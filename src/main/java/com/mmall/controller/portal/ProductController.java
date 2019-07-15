package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.controller.BaseController;
import com.mmall.service.IProductService;
import com.mmall.vo.ProductDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 商品模块
 */
@RestController
@RequestMapping("/product")
public class ProductController extends BaseController {

    @Autowired
    private IProductService productService;

    @RequestMapping ("/{productId}/detail")
    public ServerResponse<ProductDetailVo> detail(@PathVariable("productId")Integer productId){
        return productService.getProductDetail(productId);
    }

    @GetMapping("/list")
    public ServerResponse<PageInfo> list(@RequestParam(value = "keyword",required = false)String keyword,
                                         @RequestParam(value = "categoryId",required = false)Integer categoryId,
                                         @RequestParam(value = "pageNum" ,defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize" ,defaultValue = "10") int pageSize,
                                         @RequestParam(value = "orderBy" ,defaultValue = "10") String orderBy){
        return productService.getProductByKeywordCategory(keyword,categoryId,pageNum,pageSize,orderBy);
    }

}
