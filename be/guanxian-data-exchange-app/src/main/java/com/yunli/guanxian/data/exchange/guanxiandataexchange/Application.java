package com.yunli.guanxian.data.exchange.guanxiandataexchange;

import com.yunli.frame.common.feign.FeignConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 程序入口 过滤提供FeginCilet
 *
 * @author liuwei
 * @date 2020/3/4 2:32 下午
 * @since 1.0.0
 */
@EnableCaching
@EnableFeignClients(defaultConfiguration = FeignConfiguration.class, basePackages={"com.yunli"})
@SpringCloudApplication
public class Application {

	public static final String PACKAGE_ROOT = "com.yunli.guanxian.data.exchange.guanxiandataexchange";


	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		//SpringApplication.run(Application.class);

	}

}
