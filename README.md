# Spring Cloud Gateway学习及Demo项目

> Author: histonevon@zohomail.com
>
> Date: 2023/07/18

[TOC]

* 本文配套代码仓库：https://github.com/HistoneVon/springCloudGatewayOfficialDemo
* 原文地址：https://histonevon.top/archives/spring-cloud-gateway-demo
* 本文档根据官方示例项目：https://spring.io/guides/gs/gateway/
* 官方代码仓库：https://github.com/spring-guides/gs-gateway
* HTTPBin：
    * https://httpbin.org/
    * HTTPBin是一个开源的HTTP请求和响应服务，它允许开发人员测试和调试HTTP请求

## 创建项目

* 使用`Spring Initializr`创建项目，依赖选择`Gateway`、`Resilience4J`、`Contract Stub Runner`

![image-20230718090056902](https://histone-obs.obs.cn-southwest-2.myhuaweicloud.com/noteImg/image-20230718090056902.png)

## 创建简单路由

* Spring Cloud Gateway使用路由（`routes`）来处理服务请求，本文档中使用`HTTPBin`作为请求目标，所有请求都被路由到HTTPBin。
* 可以使用多种方式配置路由，本文档使用网关提供的Java API
* 在`src/test/java/com/example/springcloudgatewayofficialdemo/SpringCloudGatewayOfficialDemoApplicationTests.java`创建一个`RouteLocator`类型的Bean

```java
package com.example.springcloudgatewayofficialdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * @author histonevon
 */
@SpringBootApplication
public class SpringCloudGatewayOfficialDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudGatewayOfficialDemoApplication.class, args);
    }

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        return builder.routes().build();
    }

}

```

* `myRoutes`方法采用可创建路由的`RouteLocatorBuilder`，它也允许我们添加谓词（`predicates`）和过滤器（`filters`），这样我们就可以根据特定条件路由句柄根据需要更改请求与响应
* **创建路由**：当向网关发送请求`/get`时，该路由将请求到`http://httpbin.org/get`（非https），在此路由的配置中，添加一个过滤器（`filters`），在路由请求之前将`"Hello": "World"`添加到请求头

```java
    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        // return builder.routes().build();
        return builder.routes()
                .route(p -> p
                        .path("/get") // 请求路径
                        .filters(f -> f.addRequestHeader("Hello", "World")) // 拦截器
                        .uri("http://httpbin.org:80") // 统一资源定位符（Uniform Resource Identifier）
                ).build();
    }
```

* 在8080端口运行项目，向`http://localhost:8080/get`发送请求，我这里使用开源的**Httpie Desktop**发送请求，响应如下

![image-20230718133753525](https://histone-obs.obs.cn-southwest-2.myhuaweicloud.com/noteImg/image-20230718133753525.png)

```json
{
  "args": {},
  "headers": {
    "Accept": "*/*",
    "Content-Length": "0",
    "Forwarded": "proto=http;host=\"localhost:8080\";for=\"[0:0:0:0:0:0:0:1]:60612\"",
    "Hello": "World",
    "Host": "httpbin.org",
    "User-Agent": "HTTPie",
    "X-Amzn-Trace-Id": "Root=1-64b624ca-31c2fc67027c89542b850b66",
    "X-Forwarded-Host": "localhost:8080"
  },
  "origin": "0:0:0:0:0:0:0:1, 112.96.226.72",
  "url": "http://localhost:8080/get"
}
```

* 注意：值为`World`的请求标头`Hello`已经在`headers`中发送
* 如果使用`https://localhost:8080/get`则会出现以下问题

![image-20230718135931597](https://histone-obs.obs.cn-southwest-2.myhuaweicloud.com/noteImg/image-20230718135931597.png)

```shell
2023-07-18 14:00:46.359  WARN 14374 --- [ctor-http-nio-7] r.netty.http.client.HttpClientConnect    : [063a4515, L:/192.168.8.5:65081 ! R:httpbin.org/54.211.216.104:80] The connection observed an error

io.netty.handler.codec.DecoderException: io.netty.handler.ssl.NotSslRecordException: not an SSL/TLS record: 485454502f312e31203430302042616420526571756573740d0a5365727665723a20617773656c622f322e300d0a446174653a205475652c203138204a756c20323032332030363a30303a343620474d540d0a436f6e74656e742d547970653a20746578742f68746d6c0d0a436f6e74656e742d4c656e6774683a203132320d0a436f6e6e656374696f6e3a20636c6f73650d0a0d0a3c68746d6c3e0d0a3c686561643e3c7469746c653e3430302042616420526571756573743c2f7469746c653e3c2f686561643e0d0a3c626f64793e0d0a3c63656e7465723e3c68313e3430302042616420526571756573743c2f68313e3c2f63656e7465723e0d0a3c2f626f64793e0d0a3c2f68746d6c3e0d0a
	at io.netty.handler.codec.ByteToMessageDecoder.callDecode(ByteToMessageDecoder.java:489) ~[netty-codec-4.1.84.Final.jar:4.1.84.Final]
	at io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:280) ~[netty-codec-4.1.84.Final.jar:4.1.84.Final]
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444) [netty-transport-4.1.84.Final.jar:4.1.84.Final]
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420) [netty-transport-4.1.84.Final.jar:4.1.84.Final]
	at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412) [netty-transport-4.1.84.Final.jar:4.1.84.Final]
	at io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1410) [netty-transport-4.1.84.Final.jar:4.1.84.Final]
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:440) [netty-transport-4.1.84.Final.jar:4.1.84.Final]
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420) [netty-transport-4.1.84.Final.jar:4.1.84.Final]
	at io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:919) [netty-transport-4.1.84.Final.jar:4.1.84.Final]
	at io.netty.channel.nio.AbstractNioByteChannel$NioByteUnsafe.read(AbstractNioByteChannel.java:166) [netty-transport-4.1.84.Final.jar:4.1.84.Final]
	at io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:788) [netty-transport-4.1.84.Final.jar:4.1.84.Final]
	at io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:724) [netty-transport-4.1.84.Final.jar:4.1.84.Final]
	at io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:650) [netty-transport-4.1.84.Final.jar:4.1.84.Final]
	at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:562) [netty-transport-4.1.84.Final.jar:4.1.84.Final]
	at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:997) [netty-common-4.1.84.Final.jar:4.1.84.Final]
	at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74) [netty-common-4.1.84.Final.jar:4.1.84.Final]
	at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30) [netty-common-4.1.84.Final.jar:4.1.84.Final]
	at java.lang.Thread.run(Thread.java:750) [na:1.8.0_321]
Caused by: io.netty.handler.ssl.NotSslRecordException: not an SSL/TLS record: 485454502f312e31203430302042616420526571756573740d0a5365727665723a20617773656c622f322e300d0a446174653a205475652c203138204a756c20323032332030363a30303a343620474d540d0a436f6e74656e742d547970653a20746578742f68746d6c0d0a436f6e74656e742d4c656e6774683a203132320d0a436f6e6e656374696f6e3a20636c6f73650d0a0d0a3c68746d6c3e0d0a3c686561643e3c7469746c653e3430302042616420526571756573743c2f7469746c653e3c2f686561643e0d0a3c626f64793e0d0a3c63656e7465723e3c68313e3430302042616420526571756573743c2f68313e3c2f63656e7465723e0d0a3c2f626f64793e0d0a3c2f68746d6c3e0d0a
	at io.netty.handler.ssl.SslHandler.decodeJdkCompatible(SslHandler.java:1215) ~[netty-handler-4.1.84.Final.jar:4.1.84.Final]
	Suppressed: reactor.core.publisher.FluxOnAssembly$OnAssemblyException: 
Error has been observed at the following site(s):
	*__checkpoint ⇢ org.springframework.cloud.gateway.filter.WeightCalculatorWebFilter [DefaultWebFilterChain]
	*__checkpoint ⇢ HTTP GET "/get" [ExceptionHandlingWebHandler]
Original Stack Trace:
		at io.netty.handler.ssl.SslHandler.decodeJdkCompatible(SslHandler.java:1215) ~[netty-handler-4.1.84.Final.jar:4.1.84.Final]
		at io.netty.handler.ssl.SslHandler.decode(SslHandler.java:1285) ~[netty-handler-4.1.84.Final.jar:4.1.84.Final]
		at io.netty.handler.codec.ByteToMessageDecoder.decodeRemovalReentryProtection(ByteToMessageDecoder.java:519) ~[netty-codec-4.1.84.Final.jar:4.1.84.Final]
		at io.netty.handler.codec.ByteToMessageDecoder.callDecode(ByteToMessageDecoder.java:458) ~[netty-codec-4.1.84.Final.jar:4.1.84.Final]
		at io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:280) ~[netty-codec-4.1.84.Final.jar:4.1.84.Final]
		at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444) [netty-transport-4.1.84.Final.jar:4.1.84.Final]
		at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420) [netty-transport-4.1.84.Final.jar:4.1.84.Final]
		at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412) [netty-transport-4.1.84.Final.jar:4.1.84.Final]
		at io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1410) [netty-transport-4.1.84.Final.jar:4.1.84.Final]
		at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:440) [netty-transport-4.1.84.Final.jar:4.1.84.Final]
		at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420) [netty-transport-4.1.84.Final.jar:4.1.84.Final]
		at io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:919) [netty-transport-4.1.84.Final.jar:4.1.84.Final]
		at io.netty.channel.nio.AbstractNioByteChannel$NioByteUnsafe.read(AbstractNioByteChannel.java:166) [netty-transport-4.1.84.Final.jar:4.1.84.Final]
		at io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:788) [netty-transport-4.1.84.Final.jar:4.1.84.Final]
		at io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:724) [netty-transport-4.1.84.Final.jar:4.1.84.Final]
		at io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:650) [netty-transport-4.1.84.Final.jar:4.1.84.Final]
		at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:562) [netty-transport-4.1.84.Final.jar:4.1.84.Final]
		at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:997) [netty-common-4.1.84.Final.jar:4.1.84.Final]
		at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74) [netty-common-4.1.84.Final.jar:4.1.84.Final]
		at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30) [netty-common-4.1.84.Final.jar:4.1.84.Final]
		at java.lang.Thread.run(Thread.java:750) [na:1.8.0_321]
```

## 使用CircuitBreaker

* 通过`Resilience4J`使用，`CircuitBreaker`是`Resilience4J`的组件
* `Resilience4J`是一个断路器，其思想是**如果我们知道调用可能会失败或超时，则阻止对远程服务的调用**。这是为了我们的远程服务不必浪费关键资源，这样的退出也给了远程服务一些时间来恢复。
* 通过跟踪对远程服务发出的先前请求的结果，我们就会知道一个调用是否会失败 。例如，如果前10次调用中有8次导致失败或超时，则下一次调用也很可能会失败。
* 断路器通过包装对远程服务的调用来跟踪响应
    * 在正常运行期间，当远程服务成功响应时，我们说断路器处于“闭合”状态，断路器正常将请求传递给远程服务。
    * 当远程服务返回**错误**或**超时**时，断路器会增加一个**内部计数器**。如果错误计数超过配置的**阈值**，断路器将切换到“断开”状态，断路器立即向调用者返回错误，甚至无需尝试远程调用。
    * 经过一段**配置的时间**后，断路器从断开状态切换到“半开”状态。在这种状态下，它允许一些请求传递到远程服务以**检查**它是否仍然不可用或缓慢。 如果错误率或慢呼叫率高于配置的阈值，则切换回断开状态。但是，如果错误率或慢呼叫率低于配置的阈值，则切换到关闭状态以恢复正常操作。
* 使用HTTPBin的延迟API，它在发送响应之前等待一定的秒数。由于此API可能需要很长时间才能发送其响应，因此我们可以将使用此API的路由包装在断路器中

* 按照官网Guide，添加依赖，否则不行（我看漏了，浪费了一些时间）

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
</dependency>
```

* 按照官网Guide，添加路由

```java
@SpringBootApplication
public class SpringCloudGatewayOfficialDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudGatewayOfficialDemoApplication.class, args);
    }

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        // return builder.routes().build();
        return builder.routes()
                .route(p -> p
                        .path("/get") // 请求路径
                        .filters(f -> f.addRequestHeader("Hello", "World")) // 拦截器
                        .uri("http://httpbin.org:80")) // 统一资源定位符（Uniform Resource Identifier）
                .route(p -> p
                        .host("*.circuitbreaker.com") //只要主机是 *.circuitbreaker.com ，就将请求路由到httpbin
                        .filters(f -> f.circuitBreaker(config -> config.setName("mycmd"))) // 使用配置对象设置circuitBreaker，此处设置名称
                        .uri("http://httpbin.org:80"))
                .build();
    }

}
```

* 请求地址：`http://localhost:8080/delay/3`，注意在请求头中修改`Host`为`www.circuitbreaker.com`
* 请求结果如下，出现`504 Gateway Timeout`，即**断路器在等待来自 HTTPBin 的响应时超时**

![image-20230718165239580](./Spring Cloud Gateway学习及Demo项目.assets/image-20230718165239580.png)

* 当断路器超时，可以选择回退（`fallback`），以便客户端不会收到更有意义的东西。例如，在生产方案中，缓存中返回一些数据，本文档中返回带有正文的响应
* 设置超时使用的url

```java
@Bean
public RouteLocator myRoutes(RouteLocatorBuilder builder) {
    // return builder.routes().build();
    return builder.routes()
        .route(p -> p
               .path("/get") // 请求路径
               .filters(f -> f.addRequestHeader("Hello", "World")) // 拦截器
               .uri("http://httpbin.org:80")) // 统一资源定位符（Uniform Resource Identifier）
        .route(p -> p
               .host("*.circuitbreaker.com") //只要主机是 *.circuitbreaker.com ，就将请求路由到httpbin
               .filters(f -> f.circuitBreaker(config -> config
                                              .setName("mycmd") // 使用配置对象设置circuitBreaker，此处设置名称
                                              .setFallbackUri("forward:/fallback"))) // 设置超时时使用的url
               .uri("http://httpbin.org:80"))
        .build();
}
```

* 当断路器包装的路由超时时，它会在网关应用程序中调用`/fallback`。现在我们可以将`/fallback`端点（endpoint）添加到我们的应用程序中
* 给执行类添加`RestController`注解，并在执行类中添加如下内容

```java
@RequestMapping("/fallback")
public Mono<String> fallback() {
    return Mono.just("fallback");
}
```

* 重启服务，重新请求`http://localhost:8080/delay/3`，获得响应正文`fallback`

![image-20230719083616692](https://histone-obs.obs.cn-southwest-2.myhuaweicloud.com/noteImg/image-20230719083616692.png)

## 使用可配置的URI

* 刚才的URI是写死在代码里的，现在改成可配置的
* 在`src/main/java/com/example/springcloudgatewayofficialdemo/SpringCloudGatewayOfficialDemoApplication.java`中添加`UriConfiguration`类
* 为启用`ConfigurationProperties`，在主类中添加注解`@EnableConfigurationProperties(UriConfiguration.class)`

```java
@ConfigurationProperties
class UriConfiguration {

    private String httpBin = "http://httpbin.org:80";

    public String getHttpBin() {
        return httpBin;
    }

    public void setHttpBin(String httpBin) {
        this.httpBin = httpBin;
    }
}
```

* 在方法中使用配置类

```java
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
```

* 请求测试正常

![image-20230719112034566](https://histone-obs.obs.cn-southwest-2.myhuaweicloud.com/noteImg/image-20230719112034566.png)

## 编写测试

* 修改`src/test/java/com/example/springcloudgatewayofficialdemo/SpringCloudGatewayOfficialDemoApplicationTests.java`

```java
package com.example.springcloudgatewayofficialdemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"httpbin=http://localhost:${wiremock.server.port}"})
@AutoConfigureWireMock(port = 0)
class SpringCloudGatewayOfficialDemoApplicationTests {

    @Autowired
    private WebTestClient webClient;

    @Test
    public void contextLoads() throws Exception {
        //Stubs
        stubFor(get(urlEqualTo("/get"))
                .willReturn(aResponse()
                        .withBody("{\"headers\":{\"Hello\":\"World\"}}")
                        .withHeader("Content-Type", "application/json")));
        stubFor(get(urlEqualTo("/delay/3"))
                .willReturn(aResponse()
                        .withBody("no fallback")
                        .withFixedDelay(3000)));

        webClient
                .get().uri("/get")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.headers.Hello").isEqualTo("World");

        webClient
                .get().uri("/delay/3")
                .header("Host", "www.circuitbreaker.com")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(
                        response -> assertThat(response.getResponseBody()).isEqualTo("fallback".getBytes()));
    }
}

```

* 测试利用了Spring Cloud Contract的WireMock，建立了一个可以模拟HTTPBin的API的服务器。注意`@AutoConfigureWireMock(port = 0)`，这个注解为我们在一个随机端口上启动 WireMock
* 接下来利用 `UriConfiguration` 类，将`@SpringBootTest`注释中的 httpbin 属性设置为本地运行的 WireMock 服务器。在测试中，我们为通过网关调用的 HTTPBin API 设置“存根”，并模拟我们预期的行为。
* 运行测试，使用WebTestClient向网关发出请求并验证响应，测试结果如下

```shell
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::               (v2.6.13)

2023-07-19 16:15:05.572  INFO 58630 --- [           main] CloudGatewayOfficialDemoApplicationTests : Starting SpringCloudGatewayOfficialDemoApplicationTests using Java 1.8.0_321 on fengchalindeAir with PID 58630 (started by histonevon in /Users/histonevon/Workspace/JavaProjects/springCloudGatewayOfficialDemo)
2023-07-19 16:15:05.575  INFO 58630 --- [           main] CloudGatewayOfficialDemoApplicationTests : No active profile set, falling back to 1 default profile: "default"
2023-07-19 16:15:07.321  INFO 58630 --- [           main] o.s.cloud.context.scope.GenericScope     : BeanFactory id=e1c42f1f-fe4b-30c3-9a66-e224bc61214b
2023-07-19 16:15:08.806  INFO 58630 --- [           main] wiremock.org.eclipse.jetty.util.log      : Logging initialized @5245ms to wiremock.org.eclipse.jetty.util.log.Slf4jLog
2023-07-19 16:15:09.122  INFO 58630 --- [           main] w.org.eclipse.jetty.server.Server        : jetty-9.4.46.v20220331; built: 2022-03-31T16:38:08.030Z; git: bc17a0369a11ecf40bb92c839b9ef0a8ac50ea18; jvm 1.8.0_321-b07
2023-07-19 16:15:09.147  INFO 58630 --- [           main] w.o.e.j.server.handler.ContextHandler    : Started w.o.e.j.s.ServletContextHandler@29d405e6{/__admin,null,AVAILABLE}
2023-07-19 16:15:09.165  INFO 58630 --- [           main] w.o.e.j.s.handler.ContextHandler.ROOT    : RequestHandlerClass from context returned com.github.tomakehurst.wiremock.http.StubRequestHandler. Normalized mapped under returned 'null'
2023-07-19 16:15:09.166  INFO 58630 --- [           main] w.o.e.j.server.handler.ContextHandler    : Started w.o.e.j.s.ServletContextHandler@4fba8eec{/,null,AVAILABLE}
2023-07-19 16:15:09.191  INFO 58630 --- [           main] w.o.e.jetty.server.AbstractConnector     : Started NetworkTrafficServerConnector@662e682a{HTTP/1.1, (http/1.1, h2c)}{0.0.0.0:12473}
2023-07-19 16:15:09.196  INFO 58630 --- [           main] w.o.e.jetty.util.ssl.SslContextFactory   : x509=X509@58f31629(wiremock,h=[tom akehurst],a=[],w=[]) for Server@a0bf272[provider=null,keyStore=null,trustStore=null]
2023-07-19 16:15:09.237  INFO 58630 --- [           main] w.o.e.jetty.server.AbstractConnector     : Started NetworkTrafficServerConnector@839df62{SSL, (ssl, alpn, h2, http/1.1)}{0.0.0.0:14340}
2023-07-19 16:15:09.239  INFO 58630 --- [           main] w.org.eclipse.jetty.server.Server        : Started @5679ms
2023-07-19 16:15:10.720  INFO 58630 --- [           main] o.s.c.g.r.RouteDefinitionRouteLocator    : Loaded RoutePredicateFactory [After]
2023-07-19 16:15:10.721  INFO 58630 --- [           main] o.s.c.g.r.RouteDefinitionRouteLocator    : Loaded RoutePredicateFactory [Before]
2023-07-19 16:15:10.721  INFO 58630 --- [           main] o.s.c.g.r.RouteDefinitionRouteLocator    : Loaded RoutePredicateFactory [Between]
2023-07-19 16:15:10.721  INFO 58630 --- [           main] o.s.c.g.r.RouteDefinitionRouteLocator    : Loaded RoutePredicateFactory [Cookie]
2023-07-19 16:15:10.721  INFO 58630 --- [           main] o.s.c.g.r.RouteDefinitionRouteLocator    : Loaded RoutePredicateFactory [Header]
2023-07-19 16:15:10.721  INFO 58630 --- [           main] o.s.c.g.r.RouteDefinitionRouteLocator    : Loaded RoutePredicateFactory [Host]
2023-07-19 16:15:10.721  INFO 58630 --- [           main] o.s.c.g.r.RouteDefinitionRouteLocator    : Loaded RoutePredicateFactory [Method]
2023-07-19 16:15:10.721  INFO 58630 --- [           main] o.s.c.g.r.RouteDefinitionRouteLocator    : Loaded RoutePredicateFactory [Path]
2023-07-19 16:15:10.721  INFO 58630 --- [           main] o.s.c.g.r.RouteDefinitionRouteLocator    : Loaded RoutePredicateFactory [Query]
2023-07-19 16:15:10.721  INFO 58630 --- [           main] o.s.c.g.r.RouteDefinitionRouteLocator    : Loaded RoutePredicateFactory [ReadBody]
2023-07-19 16:15:10.721  INFO 58630 --- [           main] o.s.c.g.r.RouteDefinitionRouteLocator    : Loaded RoutePredicateFactory [RemoteAddr]
2023-07-19 16:15:10.721  INFO 58630 --- [           main] o.s.c.g.r.RouteDefinitionRouteLocator    : Loaded RoutePredicateFactory [XForwardedRemoteAddr]
2023-07-19 16:15:10.721  INFO 58630 --- [           main] o.s.c.g.r.RouteDefinitionRouteLocator    : Loaded RoutePredicateFactory [Weight]
2023-07-19 16:15:10.722  INFO 58630 --- [           main] o.s.c.g.r.RouteDefinitionRouteLocator    : Loaded RoutePredicateFactory [CloudFoundryRouteService]
2023-07-19 16:15:12.102  INFO 58630 --- [           main] o.s.b.web.embedded.netty.NettyWebServer  : Netty started on port 54887
2023-07-19 16:15:12.177  INFO 58630 --- [           main] CloudGatewayOfficialDemoApplicationTests : Started SpringCloudGatewayOfficialDemoApplicationTests in 7.14 seconds (JVM running for 8.618)
2023-07-19 16:15:13.125  INFO 58630 --- [tp1092023914-39] WireMock                                 : Request received:
127.0.0.1 - GET /get

Accept-Encoding: [gzip]
User-Agent: [ReactorNetty/1.0.24]
Accept: [*/*]
WebTestClient-Request-Id: [1]
Hello: [World]
Forwarded: [proto=http;host="localhost:54887";for="127.0.0.1:54888"]
X-Forwarded-For: [127.0.0.1]
X-Forwarded-Proto: [http]
X-Forwarded-Port: [54887]
X-Forwarded-Host: [localhost:54887]
Host: [localhost:12473]
Content-Length: [0]



Matched response definition:
{
  "status" : 200,
  "body" : "{\"headers\":{\"Hello\":\"World\"}}",
  "headers" : {
    "Content-Type" : "application/json"
  }
}

Response:
HTTP/1.1 200
Content-Type: [application/json]
Matched-Stub-Id: [75b8b8f3-46f7-4f53-84c0-f70f3aa39eaf]


2023-07-19 16:15:13.304  INFO 58630 --- [tp1092023914-18] WireMock                                 : Request received:
127.0.0.1 - GET /delay/3

Accept-Encoding: [gzip]
User-Agent: [ReactorNetty/1.0.24]
Accept: [*/*]
WebTestClient-Request-Id: [2]
Forwarded: [proto=http;host=www.circuitbreaker.com;for="127.0.0.1:54888"]
X-Forwarded-For: [127.0.0.1]
X-Forwarded-Proto: [http]
X-Forwarded-Port: [80]
X-Forwarded-Host: [www.circuitbreaker.com]
Host: [localhost:12473]
Content-Length: [0]



Matched response definition:
{
  "status" : 200,
  "body" : "no fallback",
  "fixedDelayMilliseconds" : 3000
}

Response:
HTTP/1.1 200
Matched-Stub-Id: [b9dbb210-7eca-4b77-8301-aaef4578036b]


2023-07-19 16:15:14.496  INFO 58630 --- [           main] w.o.e.jetty.server.AbstractConnector     : Stopped NetworkTrafficServerConnector@662e682a{HTTP/1.1, (http/1.1, h2c)}{0.0.0.0:12473}
2023-07-19 16:15:14.498  INFO 58630 --- [           main] w.o.e.jetty.server.AbstractConnector     : Stopped NetworkTrafficServerConnector@839df62{SSL, (ssl, alpn, h2, http/1.1)}{0.0.0.0:14340}
2023-07-19 16:15:14.499  INFO 58630 --- [           main] w.o.e.j.server.handler.ContextHandler    : Stopped w.o.e.j.s.ServletContextHandler@4fba8eec{/,null,STOPPED}
2023-07-19 16:15:14.499  INFO 58630 --- [           main] w.o.e.j.server.handler.ContextHandler    : Stopped w.o.e.j.s.ServletContextHandler@29d405e6{/__admin,null,STOPPED}
2023-07-19 16:15:15.010  INFO 58630 --- [           main] w.org.eclipse.jetty.server.Server        : jetty-9.4.46.v20220331; built: 2022-03-31T16:38:08.030Z; git: bc17a0369a11ecf40bb92c839b9ef0a8ac50ea18; jvm 1.8.0_321-b07
2023-07-19 16:15:15.015  INFO 58630 --- [           main] w.o.e.j.server.handler.ContextHandler    : Started w.o.e.j.s.ServletContextHandler@29d405e6{/__admin,null,AVAILABLE}
2023-07-19 16:15:15.016  INFO 58630 --- [           main] w.o.e.j.s.handler.ContextHandler.ROOT    : RequestHandlerClass from context returned com.github.tomakehurst.wiremock.http.StubRequestHandler. Normalized mapped under returned 'null'
2023-07-19 16:15:15.017  INFO 58630 --- [           main] w.o.e.j.server.handler.ContextHandler    : Started w.o.e.j.s.ServletContextHandler@4fba8eec{/,null,AVAILABLE}
2023-07-19 16:15:15.019  INFO 58630 --- [           main] w.o.e.jetty.server.AbstractConnector     : Started NetworkTrafficServerConnector@662e682a{HTTP/1.1, (http/1.1, h2c)}{0.0.0.0:12473}
2023-07-19 16:15:15.024  INFO 58630 --- [           main] w.o.e.jetty.util.ssl.SslContextFactory   : x509=X509@43b4ec0c(wiremock,h=[tom akehurst],a=[],w=[]) for Server@a0bf272[provider=null,keyStore=null,trustStore=null]
2023-07-19 16:15:15.038  INFO 58630 --- [           main] w.o.e.jetty.server.AbstractConnector     : Started NetworkTrafficServerConnector@839df62{SSL, (ssl, alpn, h2, http/1.1)}{0.0.0.0:14340}
2023-07-19 16:15:15.039  INFO 58630 --- [           main] w.org.eclipse.jetty.server.Server        : Started @11479ms
2023-07-19 16:15:15.087  INFO 58630 --- [ionShutdownHook] w.o.e.jetty.server.AbstractConnector     : Stopped NetworkTrafficServerConnector@662e682a{HTTP/1.1, (http/1.1, h2c)}{0.0.0.0:12473}
2023-07-19 16:15:15.089  INFO 58630 --- [ionShutdownHook] w.o.e.jetty.server.AbstractConnector     : Stopped NetworkTrafficServerConnector@839df62{SSL, (ssl, alpn, h2, http/1.1)}{0.0.0.0:14340}
2023-07-19 16:15:15.089  INFO 58630 --- [ionShutdownHook] w.o.e.j.server.handler.ContextHandler    : Stopped w.o.e.j.s.ServletContextHandler@4fba8eec{/,null,STOPPED}
2023-07-19 16:15:15.089  INFO 58630 --- [ionShutdownHook] w.o.e.j.server.handler.ContextHandler    : Stopped w.o.e.j.s.ServletContextHandler@29d405e6{/__admin,null,STOPPED}

Process finished with exit code 0

```

## 参考文献

* [Java 项目中使用 Resilience4j 框架实现隔断机制/断路器 - 知乎 (zhihu.com)](https://zhuanlan.zhihu.com/p/440113610)