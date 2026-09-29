package io.github.iweidujiang.boot2demo.consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Consumer HTTP 入口：触发 Feign / RestTemplate 出站，形成 consumer→provider 拓扑边。
 * <p>
 * 跨服务 Trace 透传（W3C）请用 {@code /call-rt}（RestTemplate）；{@code /call}（Feign）待 Agent 下一切片。
 * </p>
 *
 * @since 2026-09-07
 * @author 公众号：苏渡苇 GitHub：https://github.com/iweidujiang
 */
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
     * Feign 出站造数。当前切片<strong>尚未</strong>注入 traceparent，响应里多为 {@code traceparent=(none)}。
     *
     * @return 包装后的 Provider 响应
     */
    @GetMapping("/call")
    public String call() {
        String body = providerFeignClient.hello();
        return "consumer-feign-got: " + body;
    }

    /**
     * RestTemplate 出站造数（本切片验证入口）：应注入 {@code traceparent}，
     * Provider 回显非 {@code (none)}，且控制台同一 TraceId 含两端 SERVER。
     *
     * @return 包装后的 Provider 响应
     */
    @GetMapping("/call-rt")
    public String callRestTemplate() {
        String body = restTemplate.getForObject(providerUrl + "/api/hello", String.class);
        return "consumer-rt-got: " + body;
    }
}
