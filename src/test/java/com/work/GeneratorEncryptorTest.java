package com.work;

import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Description
 * @Author ys
 * @Date 2023/2/24 17:16
 */
@SpringBootTest
@Slf4j
public class GeneratorEncryptorTest {


    @Autowired
    private StringEncryptor stringEncryptor;

    @Test
    public void encryptor() {
        String username = stringEncryptor.encrypt("root");
        String password = stringEncryptor.encrypt("123456");
        log.info("username:{}",username);
        log.info("password:{}",password);
    }


}
