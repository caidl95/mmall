package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.controller.BaseController;
import com.mmall.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

/**
 * 订单模块
 */
@RestController
@RequestMapping("/order")
public class OrderController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IOrderService orderService;

    /**
     * 生成订单
     */
    @RequestMapping("/{shippingId}/create")
    public ServerResponse create( @PathVariable("shippingId")Integer shippingId,HttpSession session){
        return orderService.createOrder(getUidFromSession(session),shippingId);
    }

    /**
     * 取消订单
     */
    @RequestMapping("/{orderNo}/cancel")
    public ServerResponse cancel(@PathVariable("orderNo")Long orderNo,HttpSession session){
        return orderService.cancelOrder(getUidFromSession(session),orderNo);
    }

    /**
     * 获取购物车产品信息
     */
    @GetMapping("/get_order_cart_product")
    public ServerResponse getOrderCartProduct(HttpSession session){
        return orderService.getOrderCartProduct(getUidFromSession(session));
    }

    /**
     * 订单详情
     */
    @RequestMapping("/{orderNo}/detail")
    public ServerResponse detail(@PathVariable("orderNo")Long orderNo,HttpSession session){
        return orderService.getOrderDetail(getUidFromSession(session),orderNo);
    }

    /**
     * 获取订单
     */
    @GetMapping("/list")
    public ServerResponse list(HttpSession session, @RequestParam(value = "pageNum" ,defaultValue = "1") int pageNum ,@RequestParam(value = "pageSize" ,defaultValue = "10") int pageSize){
        return orderService.getOrderList(getUidFromSession(session),pageNum,pageSize);
    }

    /**
     * 订单支付
     */
    @RequestMapping("/{orderNo}/pay")
    public ServerResponse pay( @PathVariable("orderNo")Long orderNo,HttpSession session, HttpServletRequest request){
        String path = request.getSession().getServletContext().getRealPath("upload");
        return orderService.pay(orderNo,getUidFromSession(session),path);
    }

    /**
     * 支付宝回调接口
     */
    @PostMapping("/alipay_callback")
    public Object alipayCallback(HttpServletRequest request){
        Map<String,String> params = Maps.newHashMap();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator();iter.hasNext();){
            String name =(String)iter.next();
            String [] values = (String []) requestParams.get(name);
            String valueStr = "";
            for (int i=0; i<values.length;i++){
                //判断是否多元素，是的话就逗号分隔做拼接
                valueStr = (i== values.length-1)?valueStr + values[i]:valueStr +values[i]+",";
            }
            params.put(name,valueStr);
        }
        logger.info("支付宝回调，sign:{},trade_status:{},参数:{}",params.get("sign"),params.get("trade_status"),params.toString());
        //验证回调的正确性，是不是支付宝发的，并且呢还要避免重复通知。
        params.remove("sign_type");
        try {
            boolean alipayRSACheckedV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());
            if (!alipayRSACheckedV2)
                return ServerResponse.createByErrorMessage("非法请求，验证不通过，再恶意请求就报警找网管啦！！");
        } catch (AlipayApiException e) {
            logger.error("支付宝验证异常",e);

        }

        //TODO 验证各种数据

        //更新订单状态
        ServerResponse serverResponse = orderService.aliCallback(params);
        if (serverResponse.isSuccess())
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        return Const.AlipayCallback.RESPONSE_FAILED;
    }

    /**
     *  检查订单支付状态
     */
    @RequestMapping("/{orderNo}/query_order_pay")
    public ServerResponse<Boolean> queryOrderPayStatus(@PathVariable("orderNo")Long orderNo,HttpSession session){
        ServerResponse serverResponse = orderService.queryOrderPayStatus(getUidFromSession(session),orderNo);
        if (serverResponse.isSuccess())
            return ServerResponse.createBySuccess(true);
        return ServerResponse.createBySuccess(false);
    }

}
