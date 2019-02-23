package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by tttppp606 on 2019/1/25.
 */

@Service("iUserService")
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        //1、判断用户是否存在
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        //2、判断用户名和密码是否匹配：将密码转变为md5，再与数据库比较
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        //查询是否名字和密码是否匹配，还返回其他所有user数据，用于存储到session中，第一次登陆存储session
        User user = userMapper.selectLogin(username, md5Password);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误");
        }
        //3、将返回前端的密码置空，比直接写null好很多
        user.setPassword(StringUtils.EMPTY);
        //4.返回前端
        return ServerResponse.createBySuccess("登陆成功", user);
    }

    @Override
    public ServerResponse<String> register(User user) {
        //1、判断用户是否存在:复用校验方法
        ServerResponse<String> validResponse = this.checkValid(user.getUsername(), Const.USERNAME);
        if(!validResponse.isSuccess()){
            return ServerResponse.createByErrorMessage("用户已存在");
        }

        //2、判断邮箱是否存在：复用校验方法
        validResponse = this.checkValid(user.getEmail(), Const.EMAIL);
        if(!validResponse.isSuccess()){
            return ServerResponse.createByErrorMessage("邮箱已存在");
        }
        //3、将用户的角色设定为游客
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //4、密码MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        //5、user加入数据库中
        int resultCount = userMapper.insert(user);

        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    /**
     *校验
     * @param str
     * @param type
     * @return 用户名或邮箱存在，返回1；
     * @return 用户名或邮箱不存在，返回0；
     *
     */
    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        //先判断type是不是为空
        if(StringUtils.isNotBlank(type)){
            //如果是用户名，查看用户名是否存在
            if (Const.USERNAME.equals(type)) {//type写在里面，否则type为null，空指针异常
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("用户已存在");
                }
            }
            //如果是邮箱，查看邮箱是否存在
            if (Const.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("邮箱已存在");
                }
            }
        }else {
            return ServerResponse.createByErrorMessage("参数错误，type为空");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    @Override
    //返回值类型是泛型，没指定泛型的具体类型，因为返回可能是msg，也可能是question
    public ServerResponse selectQuestion(String username) {
//        先判断用户名是否存在
        ServerResponse<String> validResponse = this.checkValid(username, Const.USERNAME);
        if(validResponse.isSuccess()){
            //用户名不存在
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("该用户未设置找回密码问题,找回用户密码的提示问题为空");
    }

    /**
     *检查问题答案
     * @param username
     * @param question
     * @param answer
     * @return 成功会返回一个UUID的token，随后利用这个token读取缓存的数据
     */
    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int checkAnswer = userMapper.checkAnswer(username, question, answer);
/*      guava缓存:密码提示问题校验成功后，允许前端修改密码是有时效的。
        当修改密码时，有token，就可以改密码，没有token，就不能修改密码。
        而token是一个随机数，存在缓存中，并设置了存储在缓存中的时间*/
        if(checkAnswer > 0){
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题答案错误");
    }

    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
//        判断前端传递的forgetToken是否为空
        if(StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("参数错误，token需要传递");
        }
//        判断用户名是否存在
        ServerResponse<String> validResponse = this.checkValid(username, Const.USERNAME);
        if(validResponse.isSuccess()){
            return ServerResponse.createByErrorMessage("用户不存在");
        }
//      查看这个username的token是否过期，前端也会传来一个token，但是判断是否过期还是后端从缓存中判断原token是否过期
        String token = TokenCache.getValue(TokenCache.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("token无效或过期");
        }
//      判断前端传来的forgetToken与username的token是否一样，验证前台是用户本人
        if(StringUtils.equals(forgetToken,token)){//比str1.equals(str2);好，防止空指针
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username, md5Password);
            if(rowCount > 0){
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        }else {
            return ServerResponse.createByErrorMessage("token错误,请重新获取重置密码的token，传递的token与缓存的token不一样");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    @Override
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user) {
/*      防止横向越权，将当前用户的id（一定是id，因为id不重复）与旧密码比对，是否一致，一致才允许修改密码
        如果没有id，当前用户不断试密码，有可能会试出来密码
        如果不是id，用username，重名的用户会互相修改密码*/
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("旧密码错误");
        }
//        先把密码放入user中，再更新数据库
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
//        随后的开发中session中的user可能不包含全部的用户信息，所以用updateByPrimaryKeySelective
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount > 0){
            return ServerResponse.createBySuccessMessage("修改密码成功");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    @Override
    public ServerResponse<User> updateInformation(User user) {
        //校验新邮箱是否存在,不能用checkValid，因为当用户修改的新邮箱跟原邮箱名字一样时，还是会提示邮箱存在
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCount > 0){
            return ServerResponse.createByErrorMessage("email已存在,请更换email再尝试更新");
        }
//      user中包含了id，邮箱，问题，答案，手机，未包含名字，role，所以要用updateByPrimaryKeySelective，并且在控制层要补全user和role，保证session中user的完整
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount > 0){
            return ServerResponse.createBySuccess("更新个人信息成功",user);
        }
        return null;
    }

    @Override
    public ServerResponse getInformation(Integer id) {
        User user = userMapper.selectByPrimaryKey(id);
        if(user == null){
            return ServerResponse.createByErrorMessage("找不到当前用户，根据用户的id在数据库找不到其他信息");
        }
//        返回前端的数据不包含密码
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    /**
     * 验证是否是管理员
     * @param user
     * @return
     */
    @Override
    public ServerResponse checkAdminRole(User user) {
        if(user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }


}
