package com.leyou.filter;

import com.leyou.auth.utlis.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.config.FilterProperties;
import com.leyou.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author: HuYi.Zhang
 * @create: 2018-07-08 15:05
 **/
@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class LoginFilter extends ZuulFilter {

    @Autowired
    private JwtProperties jwtProp;

    @Autowired
    private FilterProperties filterProp;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 3;
    }

    @Override
    public boolean shouldFilter() {
        // 获取请求上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        // 获取请求路径
        String uri = request.getRequestURI();
        // 判断路径是否放行
        return !isAllowPath(uri);
    }

    private boolean isAllowPath(String uri) {
        List<String> allowPaths = filterProp.getAllowPaths();
        for (String path : allowPaths) {
            if(uri.startsWith(path)){
                return true;
            }
        }
        return false;
    }

    @Override
    public Object run() throws ZuulException {
        // 获取请求上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        // 获取用户cookie
        String token = CookieUtils.getCookieValue(request, jwtProp.getCookieName());

        // 校验token
        try {
            JwtUtils.getInfoFromToken(token, jwtProp.getPublicKey());
        } catch (Exception e) {
            // 无效的token，返回403
            ctx.setResponseStatusCode(403);
            // 不继续路由和响应
            ctx.setSendZuulResponse(false);
        }
        return null;
    }
}
