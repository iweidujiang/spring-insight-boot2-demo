# JDBC 追踪造数（Boot2 demo）。需已启动 provider(:18091) + 可选 consumer(:18090) + insight-server(:9966)
$ErrorActionPreference = "Stop"
Write-Host "== Provider 直连 =="
curl.exe -sS "http://localhost:18091/api/jdbc/fast"
Write-Host ""
curl.exe -sS "http://localhost:18091/api/jdbc/slow?ms=600"
Write-Host ""
curl.exe -sS "http://localhost:18091/api/jdbc/error"
Write-Host ""
Write-Host "== Consumer 跨服务（可选） =="
try {
  curl.exe -sS "http://localhost:18090/call-jdbc-fast"
  Write-Host ""
  curl.exe -sS "http://localhost:18090/call-jdbc-slow"
  Write-Host ""
  curl.exe -sS "http://localhost:18090/call-jdbc-error"
  Write-Host ""
} catch {
  Write-Host "(consumer 未启动，已跳过)"
}
Write-Host "约 5 秒后打开 http://localhost:9966 查看链路瀑布"
