package com.sky.mapper;

import com.sky.entity.DishFlavor;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {


    @Select("select * from user where openid = #{openid}")
    User findUser(String openid);


    void insertUser(User newUser);
}
