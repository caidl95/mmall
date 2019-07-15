package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import java.util.List;

/**
 *
 */
public interface ICategoryService {

    /**
     * 添加品类
     */
    ServerResponse addCategory(String categoryName, Integer parentId);

    /**
     * 更新CategoryName
     */
    ServerResponse updateCategoryName(Integer categoryId,String categoryName);

    /**
     * 获取Category分类
     */
    ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId);

    /**
     * 查询子当前节点的id和递归子节点的id
     */
    ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId);
}
