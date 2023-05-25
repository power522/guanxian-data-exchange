package com.yunli.guanxian.data.exchange.guanxiandataexchange.config;

import com.yunli.frame.common.component.SafeBootInterceptor;
import com.yunli.frame.common.web.CommonExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Web相关配置。
 * 这里导入了通用异常处理器，用于将异常返回内容转换为 {errorCode: "xxx" , errorMessage: "xxx"} 格式返回。
 *
 * @author: liuwei
 * @date: 2021/12/22 4:31 下午
 * @since: 1.0.0
 * @description:
 */
@Configuration
@Import({CommonExceptionHandler.class})
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Value("${crossDomain}")
    private boolean crossDomain;

    /**
     * 应用管理控制台校验token, 应用管理控制台启动时下发, yaml无需配置
     */
    @Value("${CHART_TOKEN:}")
    private String token;

    /**
     * 应用管理控制台地址, 应用管理控制台启动时下发, yaml无需配置
     */
    @Value("${APPMANAGER_URL:}")
    private String appManagerHost;

    /**
     * 设置允许跨域访问，开发阶段可能需要
     *
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (crossDomain) {
            registry.addMapping("/**")
                    .allowedOriginPatterns("*")
                    .allowCredentials(true)
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS")
                    .allowedHeaders("*")
                    .maxAge(3600);
        }

    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 增加安全启动校验拦截器, 如果校验不通过, 则所有接口均返回'license异常!'信息. 当应用从应用管理控制台部署时, 为了服务安全, 需要使用该拦截器.
        // registry.addInterceptor(safeBootInterceptor());

        if (crossDomain) {
            registry.addInterceptor(new HandlerInterceptor() {
                @Override
                public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                    // 如果是OPTIONS则结束请求
                    if (HttpMethod.OPTIONS.toString().equals(request.getMethod())) {

                        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
                        response.setHeader("Access-Control-Allow-Credentials", "true");
                        response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS");
                        response.setHeader("Access-Control-Max-Age", "86400");
                        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

                        response.setStatus(HttpStatus.NO_CONTENT.value());
                        return false;
                    }

                    return true;
                }
            });
        }
    }

    /**
     * 构建安全启动校验拦截器, 在{@link #addInterceptors(InterceptorRegistry)}中注册, 开启服务安全校验.
     *
     * @return SafeBootInterceptor
     */
    private SafeBootInterceptor safeBootInterceptor() {
        String subappName = "guanxian-data-exchange";
        String appManagerPublicKey = "-----BEGIN PUBLIC KEY-----\n" +
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAplU8V0fZhLVM64sZaKfS\n" +
                "QbzlpzEJCQ+7ZCVpvnxCtSGonZ9tuXu0Nb2n8SlwecGqYXyEsnOMejL4g6J1vzY1\n" +
                "3btiDai7RvFxGREET/tWUudYnwq2c9vL18dqZA9CmQz9OKKN7I/lng5pnqP4jXer\n" +
                "cZV7JfBuvWZjma1JnkQ8Ec2jmdbZU18AL++l1v5OXb02TH8urw6GOx7X2/4CHmWO\n" +
                "nHyQmLpcb3jhFMMG/5BL/Rijrt3N5AU4AJ1Oj8LC09UK1gQ3G1vQ5EYK/zJviQ17\n" +
                "h3IDW81oqXEmFRfjTqyxICsuaUcHiM0vcuo/x0j+yIHy6ByRZm3pk35KWR+mekGy\n" +
                "MwIDAQAB\n" +
                "-----END PUBLIC KEY-----";
        return new SafeBootInterceptor(appManagerPublicKey, subappName, token, appManagerHost);
    }

}
