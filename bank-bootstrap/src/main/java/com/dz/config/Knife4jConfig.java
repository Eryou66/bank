package com.dz.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI bankOpenApi(){
        return new OpenAPI().info(new Info()
                .title("智能银行客服系统API")
                .description("NLU 语义理解 + RAG 检索增强 + 多轮对话 + 金融级合规校验")
                .version("v0.1-M0")
                .contact(new Contact().name("洱又")));
    }

}
