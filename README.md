# spring-insight-boot2-demo

独立演示工程：**Spring Boot 2.7 + Java 8**，依赖 `spring-insight-agent-starter-boot2`，上报到本机 **insight-server:9966**。

## 前置

1. JDK 8+（建议 8 或 11）  
2. 已构建兼容线：

```bash
cd D:\a-github-project\spring-insight\boot2
mvn -DskipTests install
```

3. 已启动 insight-server（主线 Boot3 jar 即可）：

```bash
java -jar D:\a-github-project\spring-insight\insight-server\target\insight-server-0.1.0-SNAPSHOT.jar
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
```

打开 http://localhost:9966/ ：

- 服务：`boot2-demo-consumer`、`boot2-demo-provider`
- 拓扑边：`boot2-demo-consumer` → `boot2-demo-provider`（remoteService 取自 `@FeignClient(name=...)`）
- 链路：入口 SERVER + OpenFeign CLIENT

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
| boot2-demo-consumer | 18090 | `/call` → Feign → provider |
