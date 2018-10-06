package com.leyou.auth.service;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utlis.JwtUtils;
import com.leyou.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * @author: HuYi.Zhang
 * @create: 2018-07-07 18:01
 **/
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {

    @Autowired
    private JwtProperties prop;

    @Autowired
    private UserClient userClient;

    public String login(String username, String password) {
        try {
            // 校验用户名密码
            User user = this.userClient.queryUserByUsernameAndPassword(username, password);

            if (user == null) {
                return null;
            }
            // 生成token
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            String token = JwtUtils.generateToken(userInfo, prop.getPrivateKey(), prop.getExpire());

            // 返回
            return token;
        } catch (Exception e) {
            return null;
        }
    }
}
