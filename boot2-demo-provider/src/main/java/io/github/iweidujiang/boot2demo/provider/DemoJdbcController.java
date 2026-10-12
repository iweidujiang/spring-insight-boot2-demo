package io.github.iweidujiang.boot2demo.provider;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC 追踪造数（H2）：Provider {@code /api/jdbc/**}；也可由 Consumer {@code /call-jdbc-*} 跨服务触发。
 *
 * @since 2026-10-12
 * @author 公众号：苏渡苇 GitHub：https://github.com/iweidujiang
 */
@RestController
@RequestMapping("/api/jdbc")
public class DemoJdbcController {

    private final JdbcTemplate jdbcTemplate;

    public DemoJdbcController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 快 SELECT：默认阈值下不应出现 JDBC CLIENT Span。
     *
     * @return 行数据与说明
     */
    @GetMapping("/fast")
    public Map<String, Object> fast() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, name, stock FROM demo_item ORDER BY id");
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("scenario", "fast");
        body.put("rows", rows);
        body.put("hint", "默认 jdbc-slow-threshold-ms=500 时成功快查询不上报 JDBC Span");
        return body;
    }

    /**
     * 慢调用：应上报 JDBC Span。
     *
     * @param ms sleep 毫秒，默认 600
     * @return 结果说明
     */
    @GetMapping("/slow")
    public Map<String, Object> slow(@RequestParam(value = "ms", defaultValue = "600") long ms) {
        long wait = Math.max(1L, Math.min(ms, 5000L));
        jdbcTemplate.execute("CALL SLEEP_MS(" + wait + ")");
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM demo_item", Integer.class);
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("scenario", "slow");
        body.put("sleptMs", Long.valueOf(wait));
        body.put("itemCount", count);
        body.put("hint", "期望 CLIENT/JDBC Span，tag db.slow=true");
        return body;
    }

    /**
     * 错误 SQL：应始终上报。
     *
     * @return 异常摘要（HTTP 仍 200，便于 curl 看正文）
     */
    @GetMapping("/error")
    public Map<String, Object> error() {
        try {
            jdbcTemplate.queryForList("SELECT * FROM demo_item_missing");
            Map<String, Object> unexpected = new HashMap<String, Object>();
            unexpected.put("ok", Boolean.TRUE);
            return unexpected;
        } catch (Exception e) {
            Map<String, Object> body = new HashMap<String, Object>();
            body.put("scenario", "error");
            body.put("exception", e.getClass().getSimpleName());
            body.put("message", e.getMessage());
            body.put("hint", "期望 JDBC CLIENT Span status=ERROR / errorCode=SQL_ERROR");
            return body;
        }
    }

    /**
     * 快 INSERT。
     *
     * @return 插入 id
     */
    @GetMapping("/write")
    public Map<String, Object> write() {
        long id = System.currentTimeMillis() % 1000000L;
        jdbcTemplate.update("INSERT INTO demo_item (id, name, stock) VALUES (?, ?, ?)",
                Long.valueOf(id), "widget-" + id, Integer.valueOf(1));
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("scenario", "write");
        body.put("insertedId", Long.valueOf(id));
        return body;
    }
}
