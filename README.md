# spring-insight-boot2-demo

独立演示工程：**Spring Boot 2.7 + Java 8**，依赖 `spring-insight-agent-starter-boot2`，上报到本机 **insight-server:9966**。

## 前置

1. JDK 8+（建议 8 或 11）  
2. 已构建兼容线 Starter（本工程当前依赖 `0.3.0-boot2-SNAPSHOT`，**必须先 install，否则 Maven 只警告 POM missing，业务仍能启动但不会上报**）：

```bash
cd D:\a-github-project\spring-insight\boot2
mvn -DskipTests install
```

可用冒烟脚本确认 jar 已进本地仓：

```powershell
cd D:\a-github-project\spring-insight-boot2-demo
.\scripts\smoke-check.ps1
```

若用 **IntelliJ** 启动：改完依赖后务必 **Maven → Reload Project**，再重新 Run。  
曾出现过只带上空的 `spring-insight-agent-starter-boot2`、**没有** `insight-agent-boot2` 的情况——此时业务能调通，但不会埋点/上报（`/actuator/prometheus` 也没有 `spring_insight_*`）。

3. 已启动 insight-server（**主线 Boot3 Server**，与 Agent 版本线无关）

**开发联调（推荐 Docker，本机打包运行镜像，默认 sqlite）：**

```bash
cd D:\a-github-project\spring-insight
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
  ghcr.io/iweidujiang/spring-insight-server:0.1.0
```

**本机 jar（可选）：**

```bash
cd D:\a-github-project\spring-insight
mvn -pl insight-server -am package -DskipTests -Dskip.ui=true
java -jar insight-server\target\insight-server-0.1.1-SNAPSHOT.jar
```

## 启动

两个终端：

```bash
cd D:\a-github-project\spring-insight-boot2-demo
mvn -pl boot2-demo-provider spring-boot:run

cd D:\a-github-project\spring-insight-boot2-demo
mvn -pl boot2-demo-consumer spring-boot:run
```

## 冒烟脚本

先确认 Starter 已进本地仓库（不启动进程）：

```powershell
cd D:\a-github-project\spring-insight-boot2-demo
.\scripts\smoke-check.ps1
```

三个进程都起来后，可探测端点：

```powershell
.\scripts\smoke-check.ps1 -HitEndpoints
```

## 造数与验证

```bash
curl http://localhost:18090/call
curl http://localhost:18090/call-rt
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
