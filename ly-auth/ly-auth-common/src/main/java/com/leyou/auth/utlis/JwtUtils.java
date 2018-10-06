package com.leyou.auth.utlis;

import com.leyou.auth.entity.JwtConstants;
import com.leyou.auth.entity.UserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.joda.time.DateTime;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.TimeUnit;

/**
 * @author: HuYi.Zhang
 * @create: 2018-05-26 15:43
 **/
public class JwtUtils {
    /**
     * 私钥加密token
     *
     * @param userInfo      载荷中的数据
     * @param privateKey    私钥
     * @param expireMinutes 过期时间，单位秒
     * @return
     * @throws Exception
     */
    public static String generateToken(UserInfo userInfo, PrivateKey privateKey, int expireMinutes) throws Exception {
        return Jwts.builder()
                .claim(JwtConstants.JWT_KEY_ID, userInfo.getId())
                .claim(JwtConstants.JWT_KEY_USER_NAME, userInfo.getUsername())
                .setExpiration(DateTime.now().plusMinutes(expireMinutes).toDate())
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();
    }

    /**
     * 私钥加密token
     *
     * @param userInfo      载荷中的数据
     * @param privateKey    私钥字节数组
     * @param expireMinutes 过期时间，单位秒
     * @return
     * @throws Exception
     */
    public static String generateToken(UserInfo userInfo, byte[] privateKey, int expireMinutes) throws Exception {
        return Jwts.builder()
                .claim(JwtConstants.JWT_KEY_ID, userInfo.getId())
                .claim(JwtConstants.JWT_KEY_USER_NAME, userInfo.getUsername())
                .setExpiration(DateTime.now().plusMinutes(expireMinutes).toDate())
                .signWith(SignatureAlgorithm.RS256, RsaUtils.getPrivateKey(privateKey))
                .compact();
    }

    public static String generateTokenInSeconds(UserInfo userInfo, PrivateKey privateKey, int expireSeconds) throws Exception {
        return Jwts.builder()
                .claim(JwtConstants.JWT_KEY_ID, userInfo.getId())
                .claim(JwtConstants.JWT_KEY_USER_NAME, userInfo.getUsername())
                .setExpiration(DateTime.now().plusSeconds(expireSeconds).toDate())
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();
    }

    /**
     * 公钥解析token
     *
     * @param token     用户请求中的token
     * @param publicKey 公钥
     * @return
     * @throws Exception
     */
    private static Jws<Claims> parserToken(String token, PublicKey publicKey) {
        return Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token);
    }

    /**
     * 公钥解析token
     *
     * @param token     用户请求中的token
     * @param publicKey 公钥字节数组
     * @return
     * @throws Exception
     */
    private static Jws<Claims> parserToken(String token, byte[] publicKey) throws Exception {
        return Jwts.parser().setSigningKey(RsaUtils.getPublicKey(publicKey))
                .parseClaimsJws(token);
    }

    /**
     * 获取token中的用户信息
     *
     * @param token     用户请求中的令牌
     * @param publicKey 公钥
     * @return 用户信息
     * @throws Exception
     */
    public static UserInfo getInfoFromToken(String token, PublicKey publicKey) throws Exception {
        Jws<Claims> claimsJws = parserToken(token, publicKey);
        Claims body = claimsJws.getBody();
        return new UserInfo(
                ObjectUtils.toLong(body.get(JwtConstants.JWT_KEY_ID)),
                ObjectUtils.toString(body.get(JwtConstants.JWT_KEY_USER_NAME))
        );
    }

    /**
     * 获取token中的用户信息
     *
     * @param token     用户请求中的令牌
     * @param publicKey 公钥
     * @return 用户信息
     * @throws Exception
     */
    public static UserInfo getInfoFromToken(String token, byte[] publicKey) throws Exception {
        Jws<Claims> claimsJws = parserToken(token, publicKey);
        Claims body = claimsJws.getBody();
        return new UserInfo(
                ObjectUtils.toLong(body.get(JwtConstants.JWT_KEY_ID)),
                ObjectUtils.toString(body.get(JwtConstants.JWT_KEY_USER_NAME))
        );
    }

    public static void main(String[] args) throws Exception {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(2L);
        userInfo.setUsername("Jack");
        // 私钥
//        PrivateKey privateKey = RsaUtils.getPrivateKey("D:\\heima30\\rsa\\rsa.pri");
        // 生成token
//        String token = generateToken(userInfo, privateKey, 5);

//        System.out.println(token);

        // Thread.sleep(6000);
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MiwidXNlbm5hbWUiOiJKYWNrIiwiZshwIjoxNTMwOTU2MTMxfQ.APEGqxmLm4Wp866ngiVG78rXdxIDsL4d6gzfqz9juTUCxn9eNUIc4mIanSXsGFRCpt3xEUdaaY65fDwWQ7xMvVqfUJt93clZuBume-LX06_fm39e1-fTIgz3j7VhtVLOyHIKdaUjIQtrsoxYaOXmbmePv6gszyfScGfdj-t2Avc";

        PublicKey publicKey = RsaUtils.getPublicKey("D:\\heima30\\rsa\\rsa.pub");
        UserInfo info = getInfoFromToken(token, publicKey);
        System.out.println("info.getUsername() = " + info.getUsername());
    }
}