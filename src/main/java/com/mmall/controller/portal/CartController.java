package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import com.mmall.service.ICartService;
/**
 * 购物车模块
 */
@RestController
@RequestMapping("/cart")
public class CartController extends BaseController {

    @Autowired
    private ICartService cartService;

    /**
     * 获取购物车商品
     */
    @GetMapping("/list")
    public ServerResponse list(HttpSession session){
       return cartService.list(getUidFromSession(session));
    }

    /**
     * 添加购物车商品
     */
    @PostMapping("/add")
    public ServerResponse add(HttpSession session,Integer count,Integer productId){
        return cartService.add(getUidFromSession(session),productId,count);
    }

    /**
     * 更新购物车商品
     */
    @PostMapping("/update")
    public ServerResponse update(HttpSession session,Integer count,Integer productId){
        return cartService.update(getUidFromSession(session),productId,count);
    }

    /**
     * 删除指定商品
     */
    @RequestMapping("/{productIds}/delete_product")
    public ServerResponse deleteProduct(@PathVariable("productIds")String productIds , HttpSession session){
        return cartService.deleteProduct(getUidFromSession(session),productIds);
    }

    /**
     * 全选
     */
    @PostMapping("/select_all")
    public ServerResponse selectAll(HttpSession session){
        return cartService.selectOrUnSelect(getUidFromSession(session),null,Const.Cart.CHECKED);
    }

    /**
     * 全反选
     */
    @PostMapping("/un_select_all")
    public ServerResponse nuSelectAll(HttpSession session){
        return cartService.selectOrUnSelect(getUidFromSession(session),null,Const.Cart.UN_CHECKED);
    }

    /**
     * 单独选
     */
    @RequestMapping("/{productId}/select")
    public ServerResponse select( @PathVariable("productId")Integer productId,HttpSession session ){
        return cartService.selectOrUnSelect(getUidFromSession(session),productId,Const.Cart.CHECKED);
    }

    /**
     * 单独反选
     */
    @RequestMapping("/{productId}/un_select")
    public ServerResponse nuSelect(@PathVariable("productId")Integer productId,HttpSession session ){
        return cartService.selectOrUnSelect(getUidFromSession(session),productId,Const.Cart.UN_CHECKED);
    }

    /**
     * 查询当前用户的购物车里面的产品数量，如果一个产品有10个，那么数量就是10
     */
    @GetMapping("/get_cart_product_count")
    public ServerResponse getCartProductCount(HttpSession session){
        return cartService.getCartProductCount(getUidFromSession(session));
    }

}
