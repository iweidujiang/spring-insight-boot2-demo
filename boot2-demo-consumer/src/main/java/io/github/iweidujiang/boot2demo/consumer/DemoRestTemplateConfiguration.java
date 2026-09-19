package io.github.iweidujiang.boot2demo.consumer;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Demo RestTemplate：走 Builder 以便 Insight Boot2 Customizer 注入 CLIENT Span。
 *
 * @since 2026-09-18
 * @author 公众号：苏渡苇 GitHub：https://github.com/iweidujiang
 */
@Configuration
public class DemoRestTemplateConfiguration {

    /**
     * @param builder Boot 管理的 Builder
     * @return RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
