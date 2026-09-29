# spring-insight-boot2-demo

独立演示工程：**Spring Boot 2.7 + Java 8**，依赖 `spring-insight-agent-starter-boot2`，上报到本机 **insight-server:9966**。

## 前置

1. JDK 8+（建议 8 或 11）  
2. 已构建 Boot2 Starter（本工程当前依赖 `0.3.2`，**必须先 install，否则 Maven 只警告 POM missing，业务仍能启动但不会上报**）。Boot2 Agent 在 **`2.7.x` 分支仓库根**（不再有 `boot2/` 子目录）。

### 从 `main` 切到 `2.7.x`（防覆盖，务必按序）

`spring-insight` 是**同一个工作目录**切换分支，未提交改动会被带过去或冲突。**禁止**在 `main` 有未提交 Agent 改动时直接 `checkout 2.7.x`。

```powershell
# ========== A. 先在 main 收好当前工作（二选一）==========
cd D:\a-github-project\spring-insight

# 方案 A1（推荐）：提交 main 上的改动后再切
git status
git add -A
git commit -m "feat(agent): W3C traceparent 透传切片1（Servlet + RestTemplate/RestClient）"
# 若暂时不想提交，用 A2 stash，勿混用漏步骤

# 方案 A2：暂存（含未跟踪文件）
# git stash push -u -m "wip-main-a1-w3c"

# ========== B. 切到 Boot2 线并确认干净 ==========
git checkout 2.7.x
git status
# 期望：working tree clean（若有冲突先解决，不要强行继续）

# ========== C. 在 2.7.x 上对齐透传改动并 install ==========
# （由维护者把 main 的 W3C 改动 port 到 insight-agent-boot2 后）
mvn -DskipTests install

# ========== D. 测完回到 main ==========
git status   # 2.7.x 上若有未提交改动：先 commit 或 stash
git checkout main
# 若用过 A2：
# git stash pop
```

**insight-server 始终在 `main` 构建/运行**，与 Agent 分支无关；测透传时不要用 `2.7.x` 去编 Server。

若用 **IntelliJ** 启动 demo：改完依赖后务必 **Maven → Reload Project**，再重新 Run。  
曾出现过只带上空的 `spring-insight-agent-starter-boot2`、**没有** `insight-agent-boot2` 的情况——此时业务能调通，但不会埋点/上报（`/actuator/prometheus` 也没有 `spring_insight_*`）。

3. 已启动 insight-server（**始终用 `main` 的 Boot3 Server**，与 Agent 版本线无关）

**开发联调（推荐 Docker，本机打包运行镜像，默认 sqlite）：**

```bash
cd D:\a-github-project\spring-insight
git checkout main
mvn -pl insight-server -am package -DskipTests
docker compose -f compose.dev.yaml up -d --build
curl -sS http://localhost:9966/api/v1/health
```

**已发布镜像：**

```bash
docker run --rm -p 9966:9966 \
  -e SPRING_INSIGHT_SERVER_STORAGE_MODE=file \
  -e SPRING_INSIGHT_SERVER_STORAGE_FILE_PATH=/data/spans.json \
  -v spring-insight-data:/data \
  ghcr.io/iweidujiang/spring-insight-server:0.3.2
```

**本机 jar（可选，`main` 当前为 `0.4.0-SNAPSHOT`）：**

```bash
cd D:\a-github-project\spring-insight
git checkout main
mvn -pl insight-server -am package -DskipTests -Dskip.ui=true
java -jar insight-server\target\insight-server-0.4.0-SNAPSHOT.jar
```

## 启动

两个终端：

```bash
cd D:\a-github-project\spring-insight-boot2-demo
mvn -pl boot2-demo-provider spring-boot:run

cd D:\a-github-project\spring-insight-boot2-demo
mvn -pl boot2-demo-consumer spring-boot:run
```

## 造数与验证

```bash
# Feign：本切片尚未透传，响应里多为 traceparent=(none)
curl http://localhost:18090/call

# RestTemplate：本切片验证入口 —— 应看到 traceparent=00-...（非 none）
curl http://localhost:18090/call-rt
```

### 跨服务 Trace 透传（W3C `traceparent`）

**结论：可以用本工程测。** 请走 **`/call-rt`（RestTemplate）**；`/call`（Feign）要等 Agent 下一切片。

前提：本机已 `install` 带透传能力的 `spring-insight-agent-starter-boot2`（见下方「分支切换」），且 insight-server（**main**）已在 9966。

| 步骤 | 操作 | 期望 |
|------|------|------|
| 1 | `curl http://localhost:18090/call-rt` | 响应含 `traceparent=00-...-...-01`（不是 `(none)`） |
| 2 | 对照 `curl http://localhost:18090/call` | Feign 多为 `traceparent=(none)`（切片未覆盖属正常） |
| 3 | 打开 http://localhost:9966 → 链路追踪 | 点进 consumer 的 `GET /call-rt` 那条 |
| 4 | 看瀑布 / TraceId | **同一 TraceId** 下既有 `boot2-demo-consumer` 的 SERVER/CLIENT，也有 `boot2-demo-provider` 的 SERVER |
| 5 | （对比）点进旧的 `/call` Feign 链路 | 仍可能是「各服务各一条 TraceId」 |

关闭透传复测（可选）：两边 yml 设 `spring.insight.http-trace-propagation-enabled: false` 后重启，`/call-rt` 应变回 `traceparent=(none)`。

### 告警 Webhook 收件箱（本地测 insight-server）

consumer 提供钉钉式接口：`POST /insight-alert/webhook`。  
insight-server 配置：

```text
spring.insight.server.alert.webhook-url=http://host.docker.internal:18090/insight-alert/webhook
# 本机 jar 跑 Server 时用：http://127.0.0.1:18090/insight-alert/webhook
```

查看是否收到：

```powershell
curl -s http://localhost:18090/insight-alert/recent
```

打开 http://localhost:9966/ ：

- 服务：`boot2-demo-consumer`、`boot2-demo-provider`
- 拓扑边：`boot2-demo-consumer` → `boot2-demo-provider`（Feign remoteService 优先 name；RestTemplate 为 URI host）
- 链路：入口 SERVER + OpenFeign / RestTemplate CLIENT

### 验证 Micrometer（consumer）

consumer 已加 Actuator + Prometheus。造数后：

```powershell
curl -s "http://localhost:18090/actuator/prometheus" | Select-String "spring_insight"
```

期望出现：`spring_insight_spans_accepted_total`、`spring_insight_span_seconds_*`、`spring_insight_reporter_queue_size` 等。

### 多套业务共用一台 insight-server 时拓扑怎么画？

**当前行为：一张图、全量边。** 所有上报上来的、带 `remoteService` 的 CLIENT Span 都会进同一张拓扑。  
互不调用的两套系统会显示为**两个不相连的子图**（不是“只画其中一套”）。

后续可做：按 `spring.insight.application-group` 过滤拓扑，或分多套 Server 实例隔离。

### 关于 remoteService / 要不要改 hosts？

**正式业务不需要改 hosts。**

| 接入方式 | Feign 配置 | 拓扑边上的目标 |
|----------|------------|----------------|
| 注册中心 | `@FeignClient(name = "order-service")` | 服务名 |
| URL 直连（本 demo） | `@FeignClient(name = "boot2-demo-provider", url = "http://127.0.0.1:18091")` | 优先 **name**，不是 IP |

## 模块

| 模块 | 端口 | 作用 |
|------|------|------|
| boot2-demo-provider | 18091 | 被调方 SERVER Span |
| boot2-demo-consumer | 18090 | `/call` → Feign；`/call-rt` → RestTemplate → provider |
