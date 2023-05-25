package com.yunli.guanxian.data.exchange.guanxiandataexchange;

import lombok.Data;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.ContentResultMatchers;
import org.springframework.test.web.servlet.result.CookieResultMatchers;
import org.springframework.test.web.servlet.result.JsonPathResultMatchers;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.result.ModelResultMatchers;
import org.springframework.test.web.servlet.result.StatusResultMatchers;
import org.springframework.test.web.servlet.result.ViewResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertNull;

/**
 * @author: wangyajun
 * @date: 2020/3/16 下午3:17
 * @since: 1.0.0
 * @description: 测试基本类--mockMvc
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@WebAppConfiguration
@ContextConfiguration
@Data
public class BaseSpringBootTest {

    @Autowired
    private WebApplicationContext context;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private MockMvc mockMvc;

    protected HttpHeaders headers;

    @Before
    public void init() {
        logger.info("开始测试");
        headers = new HttpHeaders();
        headers.add("Content-Type", "application/json;charset=utf-8");
        headers.add("token", "test-token");
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                //.addFilter(this.getContext().getBean(ContextMdcFilter.class))//增加过滤器
                .build();
    }

    @After
    public void after() {
        logger.info("测试结束");
    }

    @Test
    public void test() {
        assertNull("测试对象不能为空", mockMvc);
    }

    protected MockHttpServletRequestBuilder post(String url) {
        return MockMvcRequestBuilders.post(url).headers(this.headers);
    }

    protected MockHttpServletRequestBuilder get(String url) {
        return MockMvcRequestBuilders.get(url).headers(this.headers);
    }


    protected MockHttpServletRequestBuilder delete(String url) {
        return MockMvcRequestBuilders.delete(url).headers(this.headers);
    }


    protected MockHttpServletRequestBuilder put(String url) {
        return MockMvcRequestBuilders.put(url).headers(this.headers);
    }

    protected MockHttpServletRequestBuilder mutipartOfFile(String url, String filePath) throws IOException {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", Paths.get(filePath).getFileName().toString(), "*/*", Files.newInputStream(Paths.get(filePath)));
        return MockMvcRequestBuilders
                .multipart(url)
                .file(mockMultipartFile)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .headers(this.headers);
    }

    protected ViewResultMatchers view() {
        return MockMvcResultMatchers.view();
    }

    protected ModelResultMatchers model() {
        return MockMvcResultMatchers.model();
    }

    protected ResultHandler print() {
        return MockMvcResultHandlers.print();
    }

    protected StatusResultMatchers status() {
        return MockMvcResultMatchers.status();
    }

    protected ContentResultMatchers content() {
        return MockMvcResultMatchers.content();
    }

    protected CookieResultMatchers cookie() {
        return MockMvcResultMatchers.cookie();
    }

    protected JsonPathResultMatchers jsonPath(String expression, Object... args) {
        return MockMvcResultMatchers.jsonPath(expression, args);
    }

    protected <T> ResultMatcher jsonPath(String expression, Matcher<T> matcher) {
        return MockMvcResultMatchers.jsonPath(expression, matcher);
    }
}
