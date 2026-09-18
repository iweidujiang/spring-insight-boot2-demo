/**
 * Consumer HTTP 入口：触发 Feign / RestTemplate 出站，形成 consumer→provider 拓扑边。
 *
 * @since：2026-09-07
 * @author：苏渡苇 公众号：苏渡苇
 *
 * GitHub：https://github.com/iweidujiang
 */
package io.github.iweidujiang.boot2demo.consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class CallController {

    /** Feign 客户端，指向 Provider */
    private final ProviderFeignClient providerFeignClient;

    /** RestTemplate（Builder 构建，带 Insight 拦截器） */
    private final RestTemplate restTemplate;

    /** Provider 直连基址 */
    private final String providerUrl;

    /**
     * @param providerFeignClient Feign 客户端
     * @param restTemplate        RestTemplate
     * @param providerUrl         Provider 基址
     */
    public CallController(ProviderFeignClient providerFeignClient,
                          RestTemplate restTemplate,
                          @Value("${demo.provider-url}") String providerUrl) {
        this.providerFeignClient = providerFeignClient;
        this.restTemplate = restTemplate;
        this.providerUrl = providerUrl;
    }

    /**
     * 入口：SERVER Span + Feign CLIENT Span（remoteService 优先 Feign name）。
     *
     * @return 包装后的 Provider 响应
     */
    @GetMapping("/call")
    public String call() {
        // 出站 Feign：Insight Boot2 应上报 CLIENT Span 且 remoteService 为 provider host
        String body = providerFeignClient.hello();
        return "consumer-got: " + body;
    }

    /**
     * RestTemplate CLIENT 造数：{@code remoteService} 取 URI host（如 127.0.0.1）。
     *
     * @return 包装后的 Provider 响应
     */
    @GetMapping("/call-rt")
    public String callRestTemplate() {
        String body = restTemplate.getForObject(providerUrl + "/api/hello", String.class);
        return "consumer-rt-got: " + body;
    }
}
