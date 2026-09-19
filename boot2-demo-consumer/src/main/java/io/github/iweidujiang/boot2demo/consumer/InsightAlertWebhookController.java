/**
 * 本地联调用「钉钉式」告警 Webhook 收件箱：接收 insight-server POST 的 JSON。
 *
 * @since 2026-09-18
 * @author 公众号：苏渡苇 GitHub：https://github.com/iweidujiang
 */
package io.github.iweidujiang.boot2demo.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/insight-alert")
public class InsightAlertWebhookController {

    private static final Logger log = LoggerFactory.getLogger(InsightAlertWebhookController.class);
    private static final int MAX_RECENT = 50;

    private final AtomicLong receivedTotal = new AtomicLong();
    private final List<Map<String, Object>> recent =
            Collections.synchronizedList(new ArrayList<Map<String, Object>>());

    /**
     * 接收告警 JSON。
     *
     * @param body 请求体
     * @return 204
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> receive(@RequestBody(required = false) Map<String, Object> body) {
        long n = receivedTotal.incrementAndGet();
        log.info("[Demo告警Webhook] #{} 收到告警: {}", n, body);
        if (body != null) {
            synchronized (recent) {
                recent.add(0, new LinkedHashMap<String, Object>(body));
                while (recent.size() > MAX_RECENT) {
                    recent.remove(recent.size() - 1);
                }
            }
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * 查看最近收到的告警。
     *
     * @return 计数与最近若干条
     */
    @GetMapping("/recent")
    public Map<String, Object> recent() {
        Map<String, Object> out = new LinkedHashMap<String, Object>();
        out.put("service", "boot2-demo-consumer");
        out.put("receivedTotal", Long.valueOf(receivedTotal.get()));
        out.put("at", Instant.now().toString());
        synchronized (recent) {
            out.put("recent", new ArrayList<Map<String, Object>>(recent));
        }
        return out;
    }
}
