/**
 * 调用 Provider 的 Feign 客户端（url 直连，验证 Boot2 Feign CLIENT Span）。
 *
 * @since：2026-09-07
 * @author：苏渡苇 公众号：苏渡苇
 *
 * GitHub：https://github.com/iweidujiang
 */
package io.github.iweidujiang.boot2demo.consumer;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "boot2-demo-provider", url = "${demo.provider-url}")
public interface ProviderFeignClient {

    /**
     * 调用 Provider 的 /api/hello。
     *
     * @return Provider 返回文案
     */
    @GetMapping("/api/hello")
    String hello();

    /** JDBC 慢查询造数（拓扑 remoteService 用 Feign name，避免 RestTemplate 落成 127.0.0.1） */
    @GetMapping("/api/jdbc/slow")
    String jdbcSlow(@RequestParam(value = "ms", defaultValue = "600") long ms);

    @GetMapping("/api/jdbc/error")
    String jdbcError();

    @GetMapping("/api/jdbc/fast")
    String jdbcFast();
}
