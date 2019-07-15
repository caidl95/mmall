package com.mmall.controller.backend;


import com.mmall.common.ServerResponse;
import com.mmall.controller.BaseController;
import com.mmall.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;

/**
 * 后台管理
 */
@RestController
@RequestMapping("/manage/category")
public class CategoryManageController extends BaseController {


    @Autowired
    private ICategoryService categoryService;

    /**
     * 添加品类
     */
    @PostMapping("/add_category")
    public ServerResponse addCategory(HttpSession session , String categoryName, @RequestParam(value = "parentId" ,defaultValue = "0") int parentId){
        //校验是否是管理员
        if (getRoleAdminFromSession(session)){
            //增加我们处理分类的逻辑
            return categoryService.addCategory(categoryName,parentId);
        }else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 更新categoryName
     * 品类名称
     */
    @PostMapping("/set_category_name")
    public ServerResponse setCategoryName(HttpSession session,Integer categoryId,String categoryName){
        //校验是否是管理员
        if (getRoleAdminFromSession(session)){
            return categoryService.updateCategoryName(categoryId,categoryName);
        }else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 根据categoryId获取当前categoryId子节点并且是平级的无递归的Category信息
     */
    @GetMapping("/get_category")
    public ServerResponse getChildrenParallelCategory(HttpSession session,@RequestParam(value = "categoryId" ,defaultValue = "0")Integer categoryId){
        //校验是否是管理员
        if (getRoleAdminFromSession(session)){
            //查询子节点的category信息，并且不递归，保持平级
            return categoryService.getChildrenParallelCategory(categoryId);
        }else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     *查询子当前节点的id和递归子节点的id
     */
    @GetMapping("/get_deep_category")
    public ServerResponse getCategoryAndDeepChildrenCategory(HttpSession session,@RequestParam(value = "categoryId" ,defaultValue = "0")Integer categoryId){
        //校验是否是管理员
        if (getRoleAdminFromSession(session)){
            //查询子当前节点的id和递归子节点的id
            return categoryService.selectCategoryAndChildrenById(categoryId);
        }else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

}
