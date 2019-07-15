package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVo;

public interface ICartService {
    /**
     * 添加购物车商品
     */
    ServerResponse add(Integer userId, Integer productId, Integer count);

    /**
     * 更新购物车商品
     */
    ServerResponse update(Integer userId, Integer productId, Integer count);

    /**
     * 删除指定的产品
     */
    ServerResponse deleteProduct(Integer userId,String productIds);

    /**
     * 查询商品
     */
    ServerResponse list(Integer userId);

    /**
     * 全选或全取消
     */
    ServerResponse<CartVo> selectOrUnSelect(Integer userId ,Integer productId, Integer checked);

    /**
     * 获取当前用户购物车中的数量
     */
    ServerResponse<Integer> getCartProductCount(Integer userId);
}
