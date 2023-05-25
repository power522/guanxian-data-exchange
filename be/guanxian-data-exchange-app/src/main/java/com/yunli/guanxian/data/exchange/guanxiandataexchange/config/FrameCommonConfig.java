package com.yunli.guanxian.data.exchange.guanxiandataexchange.config;

import com.yunli.frame.common.utils.SpringBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * frame-common 相关配置。
 * 引入了 frame-common框架的 SpringBeanUtil, 该类用于 frame-common一些功能中得到 spring上下文
 */
@Configuration
@Import(SpringBeanUtil.class)
@Slf4j
public class FrameCommonConfig {
}
