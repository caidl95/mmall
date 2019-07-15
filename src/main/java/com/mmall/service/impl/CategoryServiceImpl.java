package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.List;
import java.util.Set;

@Service
public class CategoryServiceImpl implements ICategoryService {

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    public ServerResponse addCategory(String categoryName,Integer parentId){
        if (parentId == null || StringUtils.isBlank(categoryName))
            return ServerResponse.createByErrorMessage("添加品类参数错误");
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);//这个分类是可用的
        int rowCount = insert(category);
        if (rowCount > 0 )
            return ServerResponse.createBySuccess("添加品类成功");
        return ServerResponse.createByErrorMessage("添加品类失败");
    }

    @Override
    public ServerResponse updateCategoryName(Integer categoryId, String categoryName) {
        if (categoryId == null || StringUtils.isBlank(categoryName))
            return ServerResponse.createByErrorMessage("更新品类参数错误");
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int rowCount = updateByPrimaryKeySelective(category);
        if (rowCount > 0)
            return ServerResponse.createBySuccess("更新品类名字成功");
        return ServerResponse.createByErrorMessage("更新品类名失败");
    }

    @Override
    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId) {
        List<Category> categoryList = selectCategoryChildrenByParentId(categoryId);
        if (CollectionUtils.isEmpty(categoryList))
            logger.info("未找到当前分类的子分类");
        return ServerResponse.createBySuccess(categoryList);
    }

    @Override
    public ServerResponse selectCategoryAndChildrenById(Integer categoryId) {
        Set<Category> categorySet = Sets.newHashSet();//初始化set
        findChildCategory(categorySet,categoryId);

        List<Integer> categoryIdList = Lists.newArrayList();
        if (categoryId != null){
            for (Category category : categorySet){
                categoryIdList.add(category.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryIdList);
    }

    /**
     * 递归算法，算出子节点
     */
    private Set<Category> findChildCategory(Set<Category> categorySet,Integer categoryId){
        Category category = selectByPrimaryKey(categoryId);
        if (category != null)
            categorySet.add(category);
        //查找子节点，递归算法一点要有一个退出条件
        List<Category> categoryList = selectCategoryChildrenByParentId(categoryId);
        for (Category categoryNewest : categoryList){
            findChildCategory(categorySet,categoryNewest.getId());
        }
        return categorySet;
    }

    /**
     * 添加品类
     */
    private int insert(Category category){
        return categoryMapper.insert(category);
    }

    /**
     * 选择性更新
     */
    private int updateByPrimaryKeySelective(Category category){
        return categoryMapper.updateByPrimaryKeySelective(category);
    }

    /**
     * 查询当前分类的子分类
     */
    private List<Category> selectCategoryChildrenByParentId(Integer parentId){
        return categoryMapper.selectCategoryChildrenByParentId(parentId);
    }

    /**
     *
     */
    private Category selectByPrimaryKey(Integer categoryId){
        return categoryMapper.selectByPrimaryKey(categoryId);
    }
}
