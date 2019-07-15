package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import java.util.UUID;

/**
 * 用户模块的service实现层
 */
@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password ) {
        if (checkUsername(username) == 0 )
            return ServerResponse.createByErrorMessage("用户名不存在");
        // 密码登录MD5
        User user = selectLogin(username, MD5Util.MD5EncodeUtf8(password));
        if (user == null)
            return ServerResponse.createByErrorMessage("密码错误");
        user.setPassword(null);//隐藏数据
        return ServerResponse.createBySuccess("登录成功",user);
    }

    @Override
    public ServerResponse<User> mobileLogin(String phone,String code,String sessionPhone,String sessionCode){
        if (StringUtils.isBlank(phone))
            return ServerResponse.createByErrorMessage("手机号码不能为空！");
        User user = userMapper.selectPhoneLogin(phone);
        if (user == null)
            return ServerResponse.createByErrorMessage("还未注册！请先注册");
        if (StringUtils.isBlank(code))
            return ServerResponse.createByErrorMessage("验证码不能为空！");
        if (sessionCode == null || sessionCode == null)
            return ServerResponse.createByErrorMessage("验证码已过期！");
        if (!sessionPhone.equals(phone))
            return ServerResponse.createByErrorMessage("非法用户登录！");
        if (!sessionCode.equals(code))
            return ServerResponse.createByErrorMessage("验证码错误！");
        user.setPassword(null);//隐藏数据
        return ServerResponse.createBySuccess(user);
    }

    @Override
    public ServerResponse<String> register(User user){
        ServerResponse validResponse  = this.checkValid(user.getUsername(),Const.USERNAME);
        if (!validResponse.isSuccess())
            return validResponse;
        validResponse = this.checkValid(user.getEmail(),Const.EMAIL);
        if (!validResponse.isSuccess())
            return validResponse;
        //设置权限
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //  MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        if (insert(user) == 0)
            return ServerResponse.createByError();
        return ServerResponse.createByErrorMessage("注册成功");
    }

    @Override
    public ServerResponse<String> checkValid(String str,String type){
        if (StringUtils.isNotBlank(type)){
            //非空就开始效验
            if (Const.USERNAME.equals(type)){
                if (checkUsername(str) > 0)
                    return ServerResponse.createByErrorMessage("用户名已存在");
            }
            if (Const.EMAIL.equals(type)){
                if ((checkEmail(str)) > 0)
                    return ServerResponse.createByErrorMessage("email已存在");
            }
        } else {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    @Override
    public ServerResponse<String> selectQuestion(String username) {
        ServerResponse validResponse = this.checkValid(username,Const.USERNAME);
        if (validResponse.isSuccess()){
            //用户名不存在
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        String question = selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question))//非空
            return ServerResponse.createBySuccess(question);
        return ServerResponse.createByErrorMessage("找回密码的问题是空的");
    }

    @Override
    public ServerResponse<String> checkAnswer(String username,String question,String answer){
        int resultCount = getCheckAnswer(username,question,answer);
        if (resultCount > 0 ){
            //说名问题及问题答案是这个用户的，并且是正确的
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }

    @Override
    public ServerResponse<String> forgetRestPassword(String username, String passwordNew, String forgetToken) {
        if (StringUtils.isBlank(forgetToken))//判断是否为空
            return ServerResponse.createByErrorMessage("参数错误，token需要传递");
        ServerResponse validResponse = this.checkValid(username,Const.USERNAME);
        if (validResponse.isSuccess())//判断用户名不存在
            return ServerResponse.createByErrorMessage("用户名不存在");
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if (StringUtils.isBlank(token))
            return ServerResponse.createByErrorMessage("token无效或过期");
        if (StringUtils.equals(token,forgetToken)){
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = updatePasswordByUsername(username,md5Password);
            if (rowCount > 0)
                return ServerResponse.createBySuccessMessage("修改密码成功");
        }else {
            return ServerResponse.createByErrorMessage("token错误请重新获取重置密码的token");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    @Override
    public ServerResponse<String> resetPassword(String passwordOld,String passwordNew,User user){
        //防止横向越权，要校验一下这个用户的旧密码，一定要指定是这个用户，因为我们会查询一个count（1），如果不知道id，那么结果就是true coint>0
        int resultCount = checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if (resultCount == 0)
            return ServerResponse.createByErrorMessage("旧密码错误");
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = updateByPrimaryKeySelective(user);
        if (updateCount > 0 )
            return ServerResponse.createBySuccessMessage("更新成功");
        return ServerResponse.createByErrorMessage("密码更新失败");
    }

    @Override
    public ServerResponse<User> updateInformation(User user){
        //email也要校验，校验新的email是不是已经存在，并且存在的email如果相同的话，不能是我们当前这个用户的
        int resultCount = checkEmailByUserId(user.getEmail(),user.getId());
        if (resultCount > 0 )
            return ServerResponse.createByErrorMessage("email已经存在，请更换email在尝试更新");
        //username是不能被跟新的,此处的username是从原本登录的session中获取
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        int updateCount = updateByPrimaryKeySelective(updateUser);
        if (updateCount > 0 ){
            updateUser.setUsername(user.getUsername());
            return ServerResponse.createBySuccess("更新个人信息成功",updateUser);
        }
        return ServerResponse.createByErrorMessage("更新个人信息失败");
    }

    @Override
    public ServerResponse<User> getInformation(Integer userId) {
        User user = selectByPrimaryKey(userId);
        if (user == null)
            return ServerResponse.createByErrorMessage("找不到当前用户");
        user.setPassword(StringUtils.EMPTY);//将密码设置掉
        return ServerResponse.createBySuccess(user);
    }

    @Override
    public ServerResponse checkAdminRole(User user) {
        if (user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN)
            return ServerResponse.createBySuccess();
        return ServerResponse.createByError();
    }


    /**
     * 根据用户名查询用户数量
     * @param username
     * @return
     */
    private int checkUsername(String username){
        return userMapper.checkUsername(username);
    }

    /**
     * 根据email查询用户数量
     * @param email
     * @return
     */
    private int checkEmail(String email){
        return userMapper.checkEmail(email);
    }

    /**
     * 插入用户
     * @param user
     * @return
     */
    private int insert(User user){
        return userMapper.insert(user);
    }

    /**
     * 验证登录信息
     * @param username
     * @param password
     * @return
     */
    private User selectLogin(String username,String password){
        return userMapper.selectLogin(username,password);
    }

    /**
     * 查询用户的找回密码问题
     * @param username
     * @return
     */
    private String selectQuestionByUsername(String username){
       return userMapper.selectQuestionByUsername(username);
    }

    /**
     * 查询用户的忘记密码问题与答案
     */
    private int getCheckAnswer(String username,String question,String answer){
        return userMapper.checkAnswer(username,question,answer);
    }

    /**
     * 跟新密码
     */
    private int updatePasswordByUsername(String username,String password){
        return userMapper.updatePasswordByUsername(username,password);
    }

    /**
     *校验用户与密码是否匹配
     */
    private int checkPassword(String passwordOld,Integer userId){
        return userMapper.checkPassword(passwordOld,userId);
    }

    /**
     * 选择性更新
     */
    private int updateByPrimaryKeySelective(User user){
        return userMapper.updateByPrimaryKeySelective(user);
    }

    /**
     * 校验用户与email是否匹配,有结果则是已经被他人占用
     */
    private int checkEmailByUserId(String email,Integer userId){
        return userMapper.checkEmailByUserId(email,userId);
    }

    /**
     * 根据用户id查询用户信息
     */
    private User selectByPrimaryKey(Integer userId){
        return userMapper.selectByPrimaryKey(userId);
    }


}
