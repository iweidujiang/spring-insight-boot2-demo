/**
 * Provider HTTP 接口：供 Consumer Feign 调用，产生 SERVER Span。
 *
 * @since：2026-09-07
 * @author：苏渡苇 公众号：苏渡苇
 *
 * GitHub：https://github.com/iweidujiang
 */
package io.github.iweidujiang.boot2demo.provider;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

    /**
     * 简单问候接口。
     *
     * @return 固定文案
     */
    @GetMapping("/hello")
    public String hello() {
        // 返回固定字符串，便于 Consumer 断言与链路观察
        return "hello-from-boot2-provider";
    }
}
