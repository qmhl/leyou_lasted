package com.leyou.user.service;

import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author: HuYi.Zhang
 * @create: 2018-07-05 15:55
 **/
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;

    private static final String KEY_PREFIX = "user:code:phone:";

    public Boolean checkData(String data, Integer type) {
        User t = new User();

        switch (type) {
            case 1:
                t.setUsername(data);
                break;
            case 2:
                t.setPhone(data);
                break;
            default:
                return null;
        }

        return this.userMapper.selectCount(t) == 0;
    }

    public void sendCode(String phone) {
        String key = KEY_PREFIX + phone;
        // 1、生成验证码
        String code = NumberUtils.generateCode(6);
        // 2、存入redis
        this.redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
        // 3、发送消息
        Map<String, String> msg = new HashMap<>();
        msg.put("phone", phone);
        msg.put("code", code);
        this.amqpTemplate.convertAndSend("ly.sms.exchange", "sms.verify.code", msg);
    }

    public boolean register(User user, String code) {
        String key = KEY_PREFIX + user.getPhone();
        // 先对验证码校验
        String cacheCode = this.redisTemplate.opsForValue().get(key);
        if (!StringUtils.equals(code, cacheCode)) {
            // 验证码有误
            return false;
        }

        // 完善数据
        user.setId(null);
        user.setCreated(new Date());

        // 生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);

        // 对密码加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(), salt));

        // 保存
        int count = this.userMapper.insert(user);

        return count == 1;
    }

    public User queryUserByUsernameAndPassword(String username, String password) {
        // 校验用户名
        User t = new User();
        t.setUsername(username);
        User user = this.userMapper.selectOne(t);
        // 判断用户
        if (user == null) {
            return null;
        }
        // 判断密码是否正确
        if (!StringUtils.equals(user.getPassword(), CodecUtils.md5Hex(password, user.getSalt()))) {
            return null;
        }
        return user;
    }
}
