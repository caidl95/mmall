package com.mmall.service.impl;
import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayMonitorService;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayMonitorServiceImpl;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.service.impl.AlipayTradeWithHBServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderProductVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;

/**
 * 支付宝支付接口
 */
@Service
public class OrderServiceImpl implements IOrderService {

    static {
        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");
        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
        // 支付宝当面付2.0服务（集成了交易保障接口逻辑）
        tradeWithHBService = new AlipayTradeWithHBServiceImpl.ClientBuilder().build();
        /** 如果需要在程序中覆盖Configs提供的默认参数, 可以使用ClientBuilder类的setXXX方法修改默认参数 否则使用代码中的默认设置 */
        monitorService = new AlipayMonitorServiceImpl.ClientBuilder()
                .setGatewayUrl("http://mcloudmonitor.com/gateway.do").setCharset("GBK")
                .setFormat("json").build();
    }

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ShippingMapper shippingMapper;

    //backend
    public ServerResponse<PageInfo> manageList(int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectAllOrder();
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList,null);
        PageInfo pageResult = new PageInfo(orderList);
        pageResult.setList(orderVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    public ServerResponse<OrderVo> manageDetail(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null)
            return ServerResponse.createByErrorMessage("订单不存在");
        List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
        OrderVo orderVo = assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    public ServerResponse<PageInfo> manageSearch(Long orderNo,int pageNum,int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null)
            return ServerResponse.createByErrorMessage("订单不存在");
        List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
        OrderVo orderVo = assembleOrderVo(order,orderItemList);
        PageInfo pageResult = new PageInfo(Lists.newArrayList(order));
        pageResult.setList(Lists.newArrayList(orderVo));
        return ServerResponse.createBySuccess(pageResult);
    }

    public ServerResponse<String> manageSendGoods(Long orderNo){
        Order order  = orderMapper.selectByOrderNo(orderNo);
        if (order == null)
            return ServerResponse.createByErrorMessage("订单不存在");
        if (order.getStatus() == Const.OrderStatusEnum.PAID.getCode()){//判断是否已付款
            order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
            order.setSendTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
            return ServerResponse.createBySuccess("发货成功");
        }
        return ServerResponse.createByErrorMessage("订单状态错误");
    }

    /**
     * ============================== 前端====================================================
     */


    public ServerResponse<PageInfo> getOrderList(Integer userId ,int pageNum , int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList = assembleOrderVoList(orderList,userId);
        PageInfo pageResult = new PageInfo(orderList);
        pageResult.setList(orderVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    public ServerResponse getOrderDetail(Integer userId,Long orderNo){
        Order order = orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if (order == null)
            return ServerResponse.createByErrorMessage("没有找打该订单");
        List<OrderItem> orderItemList = orderItemMapper.getByOrderNoUserId(orderNo,userId);
        OrderVo orderVo = assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    public ServerResponse getOrderCartProduct(Integer userId){
        OrderProductVo orderProductVo = new OrderProductVo();
        //从购物车中获取数据
        List<Cart> cartList =cartMapper.selectCheckedCartByUserId(userId);
        ServerResponse serverResponse = this.getCartOrderItem(userId,cartList);
        if (!serverResponse.isSuccess())
            return serverResponse;
        //成功 继续 计算总结
        List<OrderItem> orderItemList =(List<OrderItem>) serverResponse.getData();
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        BigDecimal payment = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList){
            payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
            orderItemVoList.add(assembleOrderItemVo(orderItem));
        }
        orderProductVo.setProductTotalPrice(payment);
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return ServerResponse.createBySuccess(orderProductVo);
    }

    public ServerResponse<String> cancelOrder(Integer userId,Long orderNo){
        Order order = orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if (order == null)
            return ServerResponse.createByErrorMessage("该用户此订单不存在");
        if (order.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode())
            return ServerResponse.createByErrorMessage("已付款，无法取消订单");
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
        int row = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if (row > 0)
            return ServerResponse.createBySuccess();
        return ServerResponse.createByError();
    }

    public ServerResponse createOrder(Integer userId ,Integer shippingId){
        //从购物车中获取数据
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        //计算这个订单的总价
        ServerResponse serverResponse = this.getCartOrderItem(userId,cartList);
        if (!serverResponse.isSuccess())
            return serverResponse;
        List<OrderItem> orderItemsList = (List<OrderItem>)serverResponse.getData();
        BigDecimal payment = this.getOrderTotalPrice(orderItemsList);
        //生成订单
        Order order = assembleOrder(userId,shippingId,payment);
        if (order == null)
            return ServerResponse.createByErrorMessage("生成订单错误");
        if (CollectionUtils.isEmpty(orderItemsList))
            return ServerResponse.createByErrorMessage("购物车为空");
        for (OrderItem orderItem : orderItemsList){
            orderItem.setOrderNo(order.getOrderNo());
        }
        //mybatis批量插入
        orderItemMapper.batchInsert(orderItemsList);
        //生成成功，我们要减少我们产品的库存
        this.reduceProductStock(orderItemsList);
        //清空下购物车
        this.cleanCart(cartList);
        //返回给前端数据
        OrderVo orderVo = this.assembleOrderVo(order,orderItemsList);
        return ServerResponse.createBySuccess(orderVo);
    }

    public ServerResponse pay(Long orderNo , Integer userId , String path ) {
        Map<String, String> resultMap = new HashMap<>();
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null)
            return ServerResponse.createByErrorMessage("用户没有该订单");
        resultMap.put("orderNo", String.valueOf(order.getOrderNo()));

        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店消费”
        String subject = new StringBuilder().append("happymmall扫描支付，订单号：").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (必填) 付款条码，用户支付宝钱包手机app点击“付款”产生的付款条码
        String authCode = "用户自己的支付宝付款码"; // 条码示例，286648048691290423
        // (可选，根据需要决定是否使用) 订单可打折金额，可以配合商家平台配置折扣活动，如果订单部分商品参与打折，可以将部分商品总价填写至此字段，默认全部商品可打折
        // 如果该值未传入,但传入了【订单总金额】,【不可打折金额】 则该值默认为【订单总金额】- 【不可打折金额】
        //        String discountableAmount = "1.00"; //

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0.0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品3件共20.00元"
        String body = new StringBuilder().append("订单").append(outTradeNo).append("购买商品共").append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        String providerId = "2088100200300400500";
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId(providerId);

        // 支付超时，线下扫码交易定义为5分钟
        String timeoutExpress = "5m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<>();

        // 商品明细列表，需填写购买商品详细信息
        List<OrderItem> orderItemsList = orderItemMapper.getByOrderNoUserId(orderNo, userId);

        for (OrderItem orderItem : orderItemsList) {
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、商品名称、商品价格（单位为分）、商品数量
            GoodsDetail goods = GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(), new Double(100).doubleValue()).longValue(),
                    orderItem.getQuantity());
            // 创建好一个商品后添加至商品明细列表
            goodsDetailList.add(goods);
        }
        // 创建条码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);
        // 调用tradePay方法获取当面付应答
        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝支付成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);
                //创建目标文件
                File folder = new File(path);
                if (!folder.exists()){
                    folder.setWritable(true);
                    folder.mkdirs();
                }

                //需要修改为运行机器上的路径
                //细节    path 获取时未加 “/” 要自行拼接
                String qrPath = String.format(path+"/qr-%s.png",response.getOutTradeNo());
                String qrFileName = String.format("qr-%s.png",response.getOutTradeNo());
                ZxingUtils.getQRCodeImge(response.getQrCode(),256,qrPath);

                File targetFile = new File(path,qrFileName);
                //try {
                  //TODO  需要在配置文件上配置FTP  FTPUtil.uploadFile(Lists.newArrayList(targetFile));

               // } catch (IOException e) {
                //    log.error("上传二维码异常",e);
             //   }
                log.info("qrPath:"+qrPath);
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFile.getName();
                resultMap.put("qrUrl",qrUrl);
                return ServerResponse.createBySuccess(resultMap);
            case FAILED:
                log.error("支付宝支付失败!!!");
                return ServerResponse.createByErrorMessage("支付宝支付失败!!!");

            case UNKNOWN:
                log.error("系统异常，订单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，订单状态未知!!!");

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }

    public ServerResponse aliCallback(Map<String,String> params){
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null)
            return ServerResponse.createByErrorMessage("此订单非本商城订单！");
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode())
            return ServerResponse.createBySuccess("支付宝重复调用！");
        if (Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)){
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
        }
        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);

        payInfoMapper.insert(payInfo);
        return ServerResponse.createBySuccess();
    }

    public ServerResponse queryOrderPayStatus(Integer userId,Long orderNo){
        Order order = orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if (order == null)
            return ServerResponse.createByErrorMessage("用户没有该订单");
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode())
            return ServerResponse.createBySuccess();
        return ServerResponse.createByError();
    }

    /**
     * 封装List<Order>
     */
    private List<OrderVo> assembleOrderVoList(List<Order> orderList,Integer userId){
        List<OrderVo> orderVoList = Lists.newArrayList();
        for (Order order : orderList){
            List<OrderItem> orderItemList = Lists.newArrayList();
            if (userId == null){
                orderItemList = orderItemMapper.getByOrderNo(order.getOrderNo());
            }else {
                orderItemList = orderItemMapper.getByOrderNoUserId(order.getOrderNo(),userId);
            }
            OrderVo orderVo = assembleOrderVo(order,orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

    /**
     * 封装OrderVO
     */
    private OrderVo assembleOrderVo(Order order,List<OrderItem> orderItemList){
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());

        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());

        orderVo.setShippingId(order.getShippingId());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if (shipping != null) {
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(this.assembleShippingVo(shipping));
        }

        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));

        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        for (OrderItem orderItem : orderItemList){
            OrderItemVo orderItemVo = this.assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVoList);
        return orderVo;
    }

    /**
     *  封装orderItemVo
     */
    private OrderItemVo assembleOrderItemVo(OrderItem orderItem){
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());

        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        return orderItemVo;
    }

    /**
     * 封装ShippingVo
     */
    private ShippingVo assembleShippingVo(Shipping shipping){
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        return shippingVo;
    }

    /**
     *   生成订单
     */
    private Order assembleOrder(Integer userId,Integer shippingId ,BigDecimal payment){
        Order order = new Order();
        long orderNo = generateOrderNo();
        order.setOrderNo(orderNo);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        order.setPostage(0);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setPayment(payment);

        order.setUserId(userId);
        order.setShippingId(shippingId);
        //发货时间等等
        //付款时间
        int rowCount = orderMapper.insert(order);
        if (rowCount > 0)
            return order;
        return null;
    }

    /**
     * 生成订单号
     */
    private long generateOrderNo(){
        long currentTime = System.currentTimeMillis();
        return currentTime + new Random().nextInt(100);
    }

    /**
     * 订单总价
     */
    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList){
        BigDecimal payment = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList){
            payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        return payment;
    }

    /**
     * 获取购物车商品
     */
    private ServerResponse getCartOrderItem(Integer userId,List<Cart> cartList){
        List<OrderItem> orderItemList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        //校验购物车的数据，包括产品的状态和数量
        for (Cart cartItem : cartList){
            OrderItem orderItem = new OrderItem();
            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            if (Const.ProductStatusEnum.ON_SALE.getCode() != product.getStatus()){
                return ServerResponse.createByErrorMessage("产品"+product.getName()+"不是在线售卖状态");
            }
            //校验库存
            if (cartItem.getQuantity() > product.getStock()){
                return ServerResponse.createByErrorMessage("产品"+product.getName()+"库存不足");
            }
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartItem.getQuantity()));
            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);
    }

    /**
     *支付宝当面付2.0服务
     */
    private static AlipayTradeService tradeService;

    /**
     *支付宝当面付2.0服务（集成了交易保障接口逻辑）
     */
    private static AlipayTradeService   tradeWithHBService;

    /**
     *支付宝交易保障接口服务，供测试接口api使用，请先阅读readme.txt
     */
    private static AlipayMonitorService monitorService;

    /**
     *   简单打印应答
     */
    private void dumpResponse(AlipayResponse response){
        log.info(String.format("code:%s,msg:%s",response.getCode(),response.getMsg()));
        if (StringUtils.isNotEmpty(response.getSubCode()))
            log.info(String.format("subCode:%s,subMsg",response.getSubCode(),response.getSubMsg()));
        log.info("body:"+response.getBody());
    }

    /**
     *清空下购物车
     */
    private void cleanCart(List<Cart> cartList){
        for (Cart cart :cartList){
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    /**
     * 减少我们产品的库存
     */
    private void reduceProductStock(List<OrderItem> orderItemList){
        for (OrderItem orderItem : orderItemList){
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock()-orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

}
