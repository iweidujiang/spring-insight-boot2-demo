/**
 * Boot2 Demo Consumer 启动类（启用 OpenFeign）。
 *
 * @since：2026-09-07
 * @author：苏渡苇 公众号：苏渡苇
 *
 * GitHub：https://github.com/iweidujiang
 */
package io.github.iweidujiang.boot2demo.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class Boot2DemoConsumerApplication {

    /**
     * 启动 Consumer 进程。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(Boot2DemoConsumerApplication.class, args);
    }
}
