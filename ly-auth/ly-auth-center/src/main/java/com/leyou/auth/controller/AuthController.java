package com.leyou.auth.controller;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utlis.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: HuYi.Zhang
 * @create: 2018-07-07 17:59
 **/
@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties prop;

    @PostMapping("accredit")
    public ResponseEntity<Void> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest req, HttpServletResponse resp) {
        // 登录
        String token = this.authService.login(username, password);
        // 判断token
        if (StringUtils.isBlank(token)) {
            // 返回401
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // 写入cookie中
        CookieUtils.setCookie(req, resp, prop.getCookieName(), token, true);

        // 返回200
        return ResponseEntity.ok().build();
    }

    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(
            @CookieValue("LY_TOKEN") String token,
            HttpServletRequest req, HttpServletResponse resp) {
        // 解析token
        try {
            // 校验token
            UserInfo info = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            // 刷新token
            String newToken = JwtUtils.generateToken(info, prop.getPrivateKey(), prop.getExpire());
            // 写入cookie
            CookieUtils.setCookie(req, resp, prop.getCookieName(), newToken, true);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            // 返回401
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
