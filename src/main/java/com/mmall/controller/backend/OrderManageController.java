package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.controller.BaseController;
import com.mmall.service.IOrderService;
import com.mmall.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("manage/order")
public class OrderManageController extends BaseController {

    @Autowired
    private IOrderService orderService;

    /**
     * 获取所有订单
     */
    @GetMapping("/")
    public ServerResponse<PageInfo> orderList(HttpSession session,  @RequestParam(value = "pageNum" ,defaultValue = "1") int pageNum ,@RequestParam(value = "pageSize" ,defaultValue = "10") int pageSize){
        //校验是否是管理员
        if (getRoleAdminFromSession(session)){
            //增加我们处理分类的逻辑
            return orderService.manageList(pageNum,pageSize);
        }else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 查看订单详情
     */
    @RequestMapping("/{orderNo}/detail")
    public ServerResponse<OrderVo> orderDetail(@PathVariable("orderNo")Long orderNo, HttpSession session){
        //校验是否是管理员
        if (getRoleAdminFromSession(session)){
            //增加我们处理分类的逻辑
            return orderService.manageDetail(orderNo);
        }else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 按订单号搜索
     */
    @PostMapping("/search")
    public ServerResponse<PageInfo> orderSearch(HttpSession session, Long orderNo, @RequestParam(value = "pageNum" ,defaultValue = "1") int pageNum ,@RequestParam(value = "pageSize" ,defaultValue = "10") int pageSize){
        //校验是否是管理员
        if (getRoleAdminFromSession(session)){
            //增加我们处理分类的逻辑
            return orderService.manageSearch(orderNo,pageNum,pageSize);
        }else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 订单发货
     */
    @RequestMapping("/{orderNo}/send_goods")
    public ServerResponse<String> orderSendGoods(@PathVariable("orderNo")Long orderNo,HttpSession session){
        //校验是否是管理员
        if (getRoleAdminFromSession(session)){
            return orderService.manageSendGoods(orderNo);
        }else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

}
