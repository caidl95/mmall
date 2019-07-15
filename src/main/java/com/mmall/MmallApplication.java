package com.mmall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.util.unit.DataSize;

import javax.servlet.MultipartConfigElement;

@SpringBootApplication
@MapperScan("com.mmall.dao")
public class MmallApplication {

    public static void main(String[] args) {
        SpringApplication.run(MmallApplication.class, args);
    }

    @Bean
    public MultipartConfigElement multipartConfigRation() {
        MultipartConfigFactory mcf = new MultipartConfigFactory();
        DataSize maxFileSize = DataSize.ofBytes(100 * 1024 * 1024);
        DataSize maxRequestSize = DataSize.ofBytes(100 * 1024 * 1024);
        mcf.setMaxFileSize(maxFileSize);
        mcf.setMaxRequestSize(maxRequestSize);
        MultipartConfigElement mce = mcf.createMultipartConfig();
        return mce;
    }
}
