package com.work.controller;

import com.work.dto.OrderPayDTO;
import com.work.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.salt.RandomSaltGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description
 * @Author ys
 * @Date 2023/2/23 13:48
 */
@RestController
@Slf4j
public class TestController {

    @Value("${spring.profiles.active}")
    private String env;

    @Value("${account.username}")
    private String username;

    @Value("${account.password}")
    private String password;


    @GetMapping("/test")
    public String test(){
        log.info("当前环境:{},账号:{},密码:{}",env,username,password);
        return env;
    }

    @PostMapping("/export")
    public void export(HttpServletResponse httpServletResponse){
        OrderPayDTO order1 = OrderPayDTO.builder().id(1L).code("001").prePayId("1231").build();
        OrderPayDTO order2 = OrderPayDTO.builder().id(1L).code("001").prePayId("1231").build();
        OrderPayDTO order3 = OrderPayDTO.builder().id(1L).code("001").prePayId("1231").build();
        OrderPayDTO order4 = OrderPayDTO.builder().id(1L).code("001").prePayId("1231").build();
        OrderPayDTO order5 = OrderPayDTO.builder().id(1L).code("001").prePayId("1231").build();

        List<OrderPayDTO> list=new ArrayList<>();
        list.add(order1);list.add(order2);list.add(order3);list.add(order4);list.add(order5);

        ExcelUtils.exportExcel(httpServletResponse,list,OrderPayDTO.class,"测试.xlsx");

    }

}
