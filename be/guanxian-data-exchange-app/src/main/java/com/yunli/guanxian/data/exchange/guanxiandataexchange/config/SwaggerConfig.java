package com.yunli.guanxian.data.exchange.guanxiandataexchange.config;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.SpringDocConfigProperties;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.webmvc.core.SpringWebMvcProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.method.HandlerMethod;

import java.util.Map;

/**
 * Swagger 相关配置
 * @author: liuwei
 * @date: 2020/3/31 4:56 下午
 * @since: 1.0.0
 * @description: 集成swagger
 */
@Configuration
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", matchIfMissing = true)
@Slf4j
public class SwaggerConfig {
    public static final String HEADER_NS_KEY_KEY = "YL-GW-NS";
    public static final String HEADER_USER_NAME_KEY = "YL-GW-USERNAME";
    public static final String HEADER_REAL_NAME_KEY = "YL-GW-REALNAME";
    public static final String HEADER_USER_ID_KEY = "YL-GW-USERID";
    public static final String HEADER_ORG_IDS_KEY = "YL-GW-ORGIDS";
    public static final String HEADER_ROLE_IDS_KEY = "YL-GW-ROLEIDS";

    @Value("${spring.application.name}")
    private String title;

    @Value("${project.version:}")
    private String version;

    @Bean
    public OpenAPI openAPI() {
        String a ="@project.version@";
        System.out.println(a);
        return new OpenAPI().info(new Info().title(title).version(version));
    }

    @Bean
    public GroupedOpenApi exampleApi() {
        return GroupedOpenApi.builder()
                .group("其他")
                .packagesToScan("com.yunli." + title + ".example", "com.yunli." + title + ".common")
                .build();
    }

    @Bean
    public GroupedOpenApi defaultApi() {
        return GroupedOpenApi.builder()
                .group("default")
                .packagesToExclude("com.yunli." + title + ".example", "com.yunli." + title + ".common")
                .build();
    }

    /**
     * swagger的禁用缓存，默认值是false
     * springdoc:
     *   cache:
     *     disabled: true
     * @param properties
     * @return
     */
    @Bean
    public SpringWebMvcProvider springWebMvcProvider(SpringDocConfigProperties properties){
        return new SpringWebMvcProvider(){
            @Override
            public Map getHandlerMethods() {
                if(properties.isCacheDisabled()){
                    this.handlerMethods=null;
                }
                return super.getHandlerMethods();
            }
        };
    }

    @Bean
    public OperationCustomizer customGlobalHeaders() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            createParameter(operation, HEADER_USER_ID_KEY, "userId", "1001" );
            createParameter(operation, HEADER_NS_KEY_KEY, "用户中心命名空间/租户", "default");
//            createParameter(operation, HEADER_USER_NAME_KEY, "username,需要Base64编码,如'admin'为'YWRtaW4K'", "YWRtaW4K");
//            createParameter(operation, HEADER_REAL_NAME_KEY, "用户中文名，需要Base64编码,如'管理员'为'566h55CG5ZGYCg=='" , "566h55CG5ZGYCg==");
//            createParameter(operation, HEADER_ROLE_IDS_KEY, "角色ID列表", "admin,123");
//            createParameter(operation, HEADER_ORG_IDS_KEY, "所属部门ID列表", "100,200");
            return operation;
        };
    }


    private void createParameter(Operation operation, String name, String description, String exampleValue){
        operation.addParametersItem( new Parameter().in(ParameterIn.HEADER.toString()).schema(new StringSchema())
                .name(name).description(description).required(Boolean.FALSE).example(exampleValue));
    }

    /**
     * 用于本机开发测试，实际部署时不会通过网关代理出去
     */
    @Controller
    public static class IndexController{

        @GetMapping
        public String index() {
            return "redirect:/swagger-ui/index.html";
        }
    }

}
