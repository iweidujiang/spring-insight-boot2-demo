/**
 * Consumer HTTP 入口：触发 Feign 出站，形成 consumer→provider 拓扑边。
 *
 * @since：2026-09-07
 * @author：苏渡苇 公众号：苏渡苇
 *
 * GitHub：https://github.com/iweidujiang
 */
package io.github.iweidujiang.boot2demo.consumer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CallController {

    /** Feign 客户端，指向 Provider */
    private final ProviderFeignClient providerFeignClient;

    /**
     * @param providerFeignClient Feign 客户端
     */
    public CallController(ProviderFeignClient providerFeignClient) {
        this.providerFeignClient = providerFeignClient;
    }

    /**
     * 入口：SERVER Span + Feign CLIENT Span（remoteService=host）。
     *
     * @return 包装后的 Provider 响应
     */
    @GetMapping("/call")
    public String call() {
        // 出站 Feign：Insight Boot2 应上报 CLIENT Span 且 remoteService 为 provider host
        String body = providerFeignClient.hello();
        return "consumer-got: " + body;
    }
}
