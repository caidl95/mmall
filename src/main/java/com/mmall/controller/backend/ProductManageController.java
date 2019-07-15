package com.mmall.controller.backend;

import com.google.common.collect.Maps;
import com.mmall.common.ServerResponse;
import com.mmall.controller.BaseController;
import com.mmall.pojo.Product;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.util.PropertiesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/manage/product")
public class ProductManageController extends BaseController {

    @Autowired
    private IProductService productService;

    @Autowired
    private IFileService fileService;
    /**
     * 更新或新增产品
     */
    @PostMapping("/save")
    public ServerResponse productSave(HttpSession session, Product product){
        //判断是否是管理员
        if (getRoleAdminFromSession(session)){
            return productService.saveOrUpdateProduct(product);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 产品上下架，更新产品的状态
     */
    @PostMapping("/set_sale_status")
    public ServerResponse setSaleStatus(HttpSession session,Integer productId,Integer status){
        //判断权限
        if (getRoleAdminFromSession(session)){
            return productService.setSaleStatus(productId,status);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 后台获取商品
     */
    @GetMapping("/{productId}/detail")
    public ServerResponse getDetail(@PathVariable("productId")Integer productId,HttpSession session){
        if (getRoleAdminFromSession(session)){
            return productService.manageProductDetail(productId);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 后台商品列表动态功能
     */
    @GetMapping("/")
    public ServerResponse getList(HttpSession session, @RequestParam(value = "pageNum" ,defaultValue = "1") int pageNum,@RequestParam(value = "pageSize" ,defaultValue = "10") int pageSize){
        if (getRoleAdminFromSession(session)){
            return productService.getProductList(pageNum,pageSize);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 后台商品搜索功能
     */
    @GetMapping("/search")
    public ServerResponse productSearch(HttpSession session,String productName,Integer productId, @RequestParam(value = "pageNum" ,defaultValue = "1") int pageNum,@RequestParam(value = "pageSize" ,defaultValue = "10") int pageSize){
        if (getRoleAdminFromSession(session)){
           return productService.searchProduct(productName,productId,pageNum,pageSize);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 上传图片
     */
    @PostMapping("/upload")
    public ServerResponse upload(HttpSession session,@RequestParam("file")MultipartFile file, HttpServletRequest request){
        if (getRoleAdminFromSession(session)){
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = fileService.upload(file,path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            Map fileMap = Maps.newHashMap();
            fileMap.put("uri",targetFileName);
            fileMap.put("url",url);
            return ServerResponse.createBySuccess(fileMap);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 副文本图片上传
     */
    @PostMapping("/rich_text_img_upload")
    public Map richTextImgUpload(HttpSession session, @RequestParam("file")MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        Map resultMap = Maps.newHashMap();
     /*   if (user == null){
            resultMap.put("success",false);
            resultMap.put("msg","请登录");
            return resultMap;
        }*/
        //富文本中对于返回值有自己的要求，我们使用simditor所以按照simditor的要求进行返回
//        {
////            "success":true/false,
////            "msg":"error message",# optional
////                "file_path":"[real file path]"
////        }
        if (getRoleAdminFromSession(session)){
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = fileService.upload(file,path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            if (url != null){
                resultMap.put("success",true);
                resultMap.put("msg","上传成功");
                resultMap.put("file_path",url);
                response.addHeader("Access-Control-Allow-Headers","X-File-Name");
            } else {
                resultMap.put("success",true);
                resultMap.put("msg","上传失败");
            }
            return resultMap;
        } else {
            resultMap.put("success",false);
            resultMap.put("msg","无权限操作");
            return resultMap;
        }
    }

}
