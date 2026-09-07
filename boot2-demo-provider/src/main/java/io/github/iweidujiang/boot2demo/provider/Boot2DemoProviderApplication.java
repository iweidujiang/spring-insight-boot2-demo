/**
 * Boot2 Demo Provider 启动类。
 *
 * @since：2026-09-07
 * @author：苏渡苇 公众号：苏渡苇
 *
 * GitHub：https://github.com/iweidujiang
 */
package io.github.iweidujiang.boot2demo.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Boot2DemoProviderApplication {

    /**
     * 启动 Provider 进程。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(Boot2DemoProviderApplication.class, args);
    }
}
