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

## 造数与验证

可选：让拓扑边显示服务名（否则 host 为 IP）：

```text
# Windows: C:\Windows\System32\drivers\etc\hosts
127.0.0.1 boot2-demo-provider
```

```bash
curl http://localhost:18090/call
```

打开 http://localhost:9966/ ：

- 服务：`boot2-demo-consumer`、`boot2-demo-provider`
- 拓扑边：`boot2-demo-consumer` → `boot2-demo-provider`（配置了 hosts 时）
- 链路：入口 SERVER + OpenFeign CLIENT

## 模块

| 模块 | 端口 | 作用 |
|------|------|------|
| boot2-demo-provider | 18091 | 被调方 SERVER Span |
| boot2-demo-consumer | 18090 | `/call` → Feign → provider |
