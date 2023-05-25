package com.yunli.guanxian.data.exchange.guanxiandataexchange.config;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.yunli.frame.common.mapper.query.inner.CustomerJoinResultMapper;
import com.yunli.frame.common.model.IMultiTenant;
import com.yunli.frame.common.mybatis.handler.MapBlobTypeHandler;
import com.yunli.frame.common.mybatis.handler.MapClobTypeHandler;
import com.yunli.frame.common.mybatis.handler.Sm4EncryptTypeHandler;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static com.yunli.guanxian.data.exchange.guanxiandataexchange.Application.PACKAGE_ROOT;

/**
 * Mybatis配置
 *
 * @author xinghan
 * @date 2020/3/6 4:08 下午
 * @since 1.0.0
 */
@Configuration
@MapperScan(value = {PACKAGE_ROOT + ".mapper", PACKAGE_ROOT + ".*.mapper"}, annotationClass = Mapper.class)
@EnableTransactionManagement
@Slf4j
public class MybatisPlusConfig {

    public static final String HEADER_USER_ID_KEY = "YL-GW-USERID";

    public static final String HEADER_NS_KEY_KEY = "YL-GW-NS";

    /**
     * 密钥  16位
     */
    @Value("${encrypt.key:6064f8a2e2c541b4928cd4e55745ec84}")
    private String key;

    /**
     * 偏移量 16位
     */
    @Value("${encrypt.iv:bf3d2ba866e74a88b436daae19c41d9b}")
    private String iv;

    @PostConstruct
    public void init() {
        initSm4EncryptTypeHandler();
        initJacksonTypeHandler();
    }

    /**
     * Sm4EncryptTypeHandler 设置SM4加密密钥
     */
    protected void initSm4EncryptTypeHandler() {
        Sm4EncryptTypeHandler.initEncrypt(key, iv);
    }

    /**
     * JacksonTypeHandler 设置 json转换策略
     */
    protected void initJacksonTypeHandler() {
        ObjectMapper om = new ObjectMapper();
        //忽略不存在的属性，而不报错
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        //日期时间类型json转换配置
        SimpleModule module = new SimpleModule();
        LocalDateSerializer localDateSerializer = new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE);
        module.addSerializer(localDateSerializer);
        LocalDateDeserializer localDateDeserializer = new LocalDateDeserializer(DateTimeFormatter.ISO_LOCAL_DATE);
        module.addDeserializer(LocalDate.class, localDateDeserializer);

        String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
        LocalDateTimeSerializer localDateTimeSerializer = new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(dateTimeFormat));
        module.addSerializer(localDateTimeSerializer);
        LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(dateTimeFormat));
        module.addDeserializer(LocalDateTime.class, localDateTimeDeserializer);

        om.registerModule(module);
        JacksonTypeHandler.setObjectMapper(om);
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(List<InnerInterceptor> interceptorList) {
        // 参见官方文档： https://baomidou.com/pages/2976a3/
        //    多租户: TenantLineInnerInterceptor
        //    动态表名: DynamicTableNameInnerInterceptor
        //    自动分页: PaginationInnerInterceptor
        //    乐观锁: OptimisticLockerInnerInterceptor
        //    sql性能规范: IllegalSQLInnerInterceptor
        //    防止全表更新与删除: BlockAttackInnerInterceptor
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptorList.forEach(interceptor::addInnerInterceptor);
        return interceptor;
    }

    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            typeHandlerRegistry.register(new MapBlobTypeHandler());
            typeHandlerRegistry.register(new MapClobTypeHandler());
        };
    }

    private static String getTenantId() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getHeader(HEADER_NS_KEY_KEY);
    }

    /**
     * 多租户支持
     *
     * @return
     */
    @Bean
    @Order(100)
    @ConditionalOnProperty(name = "enable-multitenant", havingValue = "true")
    public TenantLineInnerInterceptor tenantLineInnerInterceptor() {
        TenantLineInnerInterceptor tenantLineInnerInterceptor = new TenantLineInnerInterceptor() {
            @Override
            public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
                                    ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
                try {
                    super.beforeQuery(executor, ms, parameter, rowBounds, resultHandler, boundSql);
                } catch (MybatisPlusException e) {
                    log.warn("mybatis-plus的多租户功能不能和中文排序共同启用，如果要使用中文排序功能，请设置spring.application.enable-multitenant=false！ " + e.getMessage());
                }
            }
        };
        tenantLineInnerInterceptor.setTenantLineHandler(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                String tenantId = MybatisPlusConfig.getTenantId();
                if (StringUtils.hasText(tenantId)) {
                    return new StringValue(tenantId);
                }
                return null;
            }

            @Override
            public boolean ignoreTable(String tableName) {
                TableInfo tableInfo = TableInfoHelper.getTableInfo(tableName);
                return tableInfo == null || !IMultiTenant.class.isAssignableFrom(tableInfo.getEntityType());
            }
        });

        return tenantLineInnerInterceptor;
    }

    @Bean
    @Order(300)
    public PaginationInnerInterceptor paginationInnerInterceptor() {
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        paginationInnerInterceptor.setMaxLimit(10000L); //允许最大分页数
        return paginationInnerInterceptor;
    }

    @Bean
    @Order(400)
    public OptimisticLockerInnerInterceptor optimisticLockerInnerInterceptor() {
        return new OptimisticLockerInnerInterceptor();
    }

    @Bean
    @Order(400)
    public BlockAttackInnerInterceptor blockAttackInnerInterceptor() {
        return new BlockAttackInnerInterceptor();
    }

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MyMetaObjectHandler();
    }

    @Bean
    public CustomerJoinResultMapper customerJoinResultMapper() {
        return new CustomerJoinResultMapper();
    }

    /**
     * 自动填充审计、版本、租户等信息。
     */
    public static class MyMetaObjectHandler implements MetaObjectHandler {
        @Override
        public void insertFill(MetaObject metaObject) {
            // 声明自动填充字段的逻辑。
            String userId = getCurrentUserId();
            LocalDateTime now = LocalDateTime.now();

            //严格填充,只针对非主键的字段,只有该表注解了fill 并且 字段名和字段属性 能匹配到才会进行填充(null 值不填充)
            //严格模式,填充策略,默认有值不覆盖,如果提供的值为null也不填充

            strictInsertFill(metaObject, "createTime", LocalDateTime.class, now); //创建时间
            strictInsertFill(metaObject, "creator", String.class, userId);       //创建人
            strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now); //修改时间
            strictInsertFill(metaObject, "modifier", String.class, userId);      //修改人

            strictInsertFill(metaObject, "version", Integer.class, 1);      //版本

            String tenantId = MybatisPlusConfig.getTenantId();
            if (StringUtils.hasText(tenantId)) {
                strictInsertFill(metaObject, "tenantId", String.class, getTenantId());  //租户
            }
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            String userId = getCurrentUserId();
            LocalDateTime now = LocalDateTime.now();

            //不论是否外部设置了属性值，均改写
            setFieldValByName("updateTime", now, metaObject);
            setFieldValByName("modifier", userId == null ? "" : userId, metaObject);
        }

        protected String getCurrentUserId() {
            try {
                return Optional.of((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                        .map(ServletRequestAttributes::getRequest)
                        .map(req -> req.getHeader(HEADER_USER_ID_KEY))
                        .orElse(null);
            } catch (Exception e) {
                log.warn("get current user id fail, message:{}", e.getMessage());
                return null;
            }
        }

    }
}
