package io.github.iweidujiang.boot2demo.provider;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Provider HTTP 接口：供 Consumer Feign / RestTemplate 调用，产生 SERVER Span。
 * <p>
 * 响应中回显入站 {@code traceparent}，便于目测跨服务透传是否生效。
 * </p>
 *
 * @since 2026-09-07
 * @author 公众号：苏渡苇 GitHub：https://github.com/iweidujiang
 */
@RestController
@RequestMapping("/api")
public class HelloController {

    /**
     * 简单问候接口；附带是否收到 W3C {@code traceparent}。
     *
     * @param request 入站请求（读透传头）
     * @return 固定文案 + traceparent 回显
     */
    @GetMapping("/hello")
    public String hello(HttpServletRequest request) {
        String tp = request.getHeader("traceparent");
        // (none) = 上游未注入或未装带透传的 Agent；有值则可在 Server 核对同 TraceId
        String echo = (tp == null || tp.trim().isEmpty()) ? "(none)" : tp.trim();
        return "hello-from-boot2-provider;traceparent=" + echo;
    }
}
