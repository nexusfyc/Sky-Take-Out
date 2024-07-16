package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtProperties jwtProperties;
    @Override
    public UserLoginVO wxLogin(UserLoginDTO userLoginDTO) {
        String openid = getOpenid(userLoginDTO.getCode());
        User user = userMapper.findUser(openid);
        if (user == null) {
            //  当前登陆用户为公众号新用户，新增用户并返回主键
            user = User.builder().openid(openid).createTime(LocalDateTime.now()).build();
            userMapper.insertUser(user);
        }
        HashMap<String, Object> payload = new HashMap<>();
        payload.put(JwtClaimsConstant.OPEN_ID, openid);
        payload.put(JwtClaimsConstant.USER_ID, user.getId());
        String jwt = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), payload);
        UserLoginVO userLoginVO = UserLoginVO
                .builder()
                .openid(openid)
                .token(jwt)
                .id(user.getId())
                .build();
        return userLoginVO;
    }

    /**
     * 获取微信登陆用户的唯一标识openid
     * @param code
     * @return
     */
    private String getOpenid(String code) {
        HashMap<String, String> queryParamsMap = new HashMap<>();
        queryParamsMap.put("appid", weChatProperties.getAppid());
        queryParamsMap.put("secret", weChatProperties.getSecret());
        queryParamsMap.put("js_code", code);
        queryParamsMap.put("grant_type", "authorization_code");
        String jsonStr = HttpClientUtil.doGet("https://api.weixin.qq.com/sns/jscode2session", queryParamsMap);
        JSONObject jsonObject = JSON.parseObject(jsonStr);
        return jsonObject.getString("openid");

    }
}
