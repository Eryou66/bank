package com.dz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
  * 启动类。
  * 必须显式指定 scanBasePackages：默认只扫 com.dz.bootstrap 包，
 * common / nlu / chat 三个模块里的 @Component、@RestControllerAdvice 全都扫不到。
 * M1 接入 Mapper 时在这里补 @MapperScan("com.dz.**.mapper")。
 */
@SpringBootApplication(scanBasePackages = "com.dz")
public class BankApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankApplication.class, args);
    }

}
