package com.mmall.dao;

import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);
//    查询用户名是否存在
    int checkUsername(String username);
//    查询用户名和密码是否匹配
    User selectLogin(@Param("username") String username, @Param("password") String password);
//    查询邮箱是否存在
    int checkEmail(String email);
//    查找密码提示问题
    String selectQuestionByUsername(String username);
//   验证用户名，提示问题，答案是否匹配
    int checkAnswer(@Param("username")String username,@Param("question") String question,@Param("answer") String ansewer);
//    更新密码
    int updatePasswordByUsername(@Param("username")String username,@Param("passwordNew") String passwordNew);

    int checkPassword(@Param("password") String password,@Param("id") int id);

    int checkEmailByUserId(@Param("email") String email, @Param("userId") Integer userId);
}