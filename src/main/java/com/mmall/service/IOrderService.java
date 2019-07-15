package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.vo.OrderVo;

import java.util.Map;

public interface IOrderService {

    /**
     * 支付宝支付接口
     */
    ServerResponse pay(Long orderNo , Integer userId , String path );

    /**
     * 支付宝回调后处理
     */
    ServerResponse aliCallback(Map<String,String> params);

    /**
     * 判断该订单是否完成
     */
    ServerResponse queryOrderPayStatus(Integer userId,Long orderNo);

    /**
     * 生成订单
     */
    ServerResponse createOrder(Integer userId ,Integer shippingId);

    /**
     * 取消订单
     */
    ServerResponse<String> cancelOrder(Integer userId,Long orderNo);

    /**
     * 获取购物车产品信息
     */
    ServerResponse getOrderCartProduct(Integer userId);

    /**
     *  获取订单详情
     */
    ServerResponse getOrderDetail(Integer userId,Long orderNo);

    /**
     * 获取订单
     */
    ServerResponse getOrderList(Integer userId,int pageNum ,int pageSize);

    /**
     * ===============================后台============================================
     */

    /**
    * 后台获取订单
     */
    ServerResponse<PageInfo> manageList(int pageNum, int pageSize);

    /**
     * 后台获取订单详情
     */
    ServerResponse<OrderVo> manageDetail(Long orderNo);

    /**
     * 后台按订单号搜索
     * TODO 可以增加模糊查询多样化查询
     */
    ServerResponse<PageInfo> manageSearch(Long orderNo,int pageNum,int pageSize);

    /**
     * 订单发货
     */
    ServerResponse<String> manageSendGoods(Long orderNo);
}
