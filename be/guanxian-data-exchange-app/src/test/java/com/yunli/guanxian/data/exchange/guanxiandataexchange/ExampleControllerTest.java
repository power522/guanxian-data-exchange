package com.yunli.guanxian.data.exchange.guanxiandataexchange;

import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.Assert.assertNull;


/**
 * @author: wangyajun
 * @date: 2020/3/16 下午5:49
 * @since: 1.0.0
 * @description: 单元测试demo
 */
public class ExampleControllerTest extends BaseSpringBootTest {

    static String URL_ROOT = "/guanxian-data-exchange/api";

    @Test
    public void test1() throws Exception {
        MvcResult mvcResult = this.getMockMvc().perform(this.get("/api/v2/example").queryParam("id", "100"))
                .andExpect(this.status().isOk())
//                .andExpect(this.content().encoding(StandardCharsets.UTF_8.name()))
                .andDo(this.print()).andReturn();
        assertNull(mvcResult);
    }
}
