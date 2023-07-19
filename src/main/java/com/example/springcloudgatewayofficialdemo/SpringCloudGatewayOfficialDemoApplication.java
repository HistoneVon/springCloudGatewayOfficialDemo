package com.example.springcloudgatewayofficialdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author histonevon
 */
@SpringBootApplication
@RestController
@EnableConfigurationProperties(UriConfiguration.class) // 启用配置类
public class SpringCloudGatewayOfficialDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudGatewayOfficialDemoApplication.class, args);
    }

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder, UriConfiguration uriConfiguration) {
        String httpUri = uriConfiguration.getHttpBin(); // 使用配置类
        // return builder.routes().build();
        return builder.routes()
                .route(p -> p
                        .path("/get") // 请求路径
                        .filters(f -> f.addRequestHeader("Hello", "World")) // 拦截器
                        .uri(httpUri)) // 使用配置类
                .route(p -> p
                        .host("*.circuitbreaker.com") //只要主机是 *.circuitbreaker.com ，就将请求路由到httpbin
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("mycmd") // 使用配置对象设置circuitBreaker，此处设置名称
                                .setFallbackUri("forward:/fallback"))) // 设置超时时使用的url
                        .uri(httpUri)) // 使用配置类
                .build();
    }

    @RequestMapping("/fallback")
    public Mono<String> fallback() {
        return Mono.just("fallback");
    }
}

@ConfigurationProperties
class UriConfiguration {

    private String httpBin = "http://httpbin.org:80"; // 统一资源定位符（Uniform Resource Identifier）

    public String getHttpBin() {
        return httpBin;
    }

    public void setHttpBin(String httpBin) {
        this.httpBin = httpBin;
    }
}