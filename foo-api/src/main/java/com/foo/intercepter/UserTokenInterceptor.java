package com.foo.intercepter;

import com.foo.common.utils.JSONResult;
import com.foo.common.utils.JsonUtils;
import com.foo.common.utils.RedisOperator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public class UserTokenInterceptor implements HandlerInterceptor {
    public static final String REDIS_USER_TOKEN = "redis_user_token";
    @Autowired
    private RedisOperator redisOperator;
    /**
     * 拦截请求，在访问之前
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userToken = request.getHeader("headerUserToken");
        String userId = request.getHeader("userId");
        if (StringUtils.isBlank(userToken)||StringUtils.isBlank(userId)){
            returnErrorResponse(response,JSONResult.errorMsg("请登录"));
            return false;
        }
        String uniqueToken = redisOperator.get(REDIS_USER_TOKEN+":"+userId);
        if (StringUtils.isBlank(uniqueToken)){
            returnErrorResponse(response,JSONResult.errorMsg("请登录"));
            return false;
        }
        if (uniqueToken.equals(userToken)){
            returnErrorResponse(response,JSONResult.errorMsg("请登录"));
            return false;
        }
        /**
         * false: 请求被驳回，验证出错
         * true: 允许放行
         */
        return true;
    }
    public void returnErrorResponse(HttpServletResponse response, JSONResult jsonResult){
        OutputStream out = null;
        try {
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/json");
            out = response.getOutputStream();
            out.write(JsonUtils.objectToJson(jsonResult).getBytes("utf-8"));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(out!=null){
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 请求访问controller之后，渲染之前
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    /**
     * 请求访问controller之后，渲染之后
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
