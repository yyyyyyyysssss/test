package com.work;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableEncryptableProperties
public class WorkSpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkSpringbootApplication.class, args);
    }

}
