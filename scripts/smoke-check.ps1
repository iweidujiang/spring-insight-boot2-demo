# Boot2 smoke check: verify local Maven install of starter-boot2; optional HTTP probes.
#
# Usage (from demo root):
#   .\scripts\smoke-check.ps1
#   .\scripts\smoke-check.ps1 -HitEndpoints

param(
    [string]$InsightBoot2Version = "0.1.0-boot2-SNAPSHOT",
    [switch]$HitEndpoints
)

$ErrorActionPreference = "Stop"
$groupPath = "io\github\iweidujiang"
$starterArtifact = "spring-insight-agent-starter-boot2"

function Find-StarterJar([string]$Version, [string]$Artifact) {
    $candidates = @(
        (Join-Path $env:USERPROFILE ".m2\repository\$groupPath\$Artifact\$Version\$Artifact-$Version.jar")
    )
    if ($env:MAVEN_REPO_LOCAL) {
        $candidates += (Join-Path $env:MAVEN_REPO_LOCAL "$groupPath\$Artifact\$Version\$Artifact-$Version.jar")
    }
    # common custom local repo on this machine
    $candidates += "D:\Java\mvn_repo\$groupPath\$Artifact\$Version\$Artifact-$Version.jar"
    foreach ($c in $candidates) {
        if (Test-Path $c) { return $c }
    }
    return $null
}

Write-Host "== Boot2 Starter GAV ==" -ForegroundColor Cyan
Write-Host ("io.github.iweidujiang:{0}:{1}" -f $starterArtifact, $InsightBoot2Version)

$jar = Find-StarterJar -Version $InsightBoot2Version -Artifact $starterArtifact
if (-not $jar) {
    Write-Host "[FAIL] starter jar not found in local Maven repos" -ForegroundColor Red
    Write-Host "Run: cd D:\a-github-project\spring-insight\boot2 ; mvn -DskipTests install"
    exit 1
}
Write-Host "[OK] installed: $jar" -ForegroundColor Green

if (-not $HitEndpoints) {
    Write-Host ""
    Write-Host "Skip HTTP probes (pass -HitEndpoints when services are up)."
    Write-Host "Order: insight-server:9966 -> provider:18091 -> consumer:18090 -> curl /call"
    exit 0
}

function Test-Url([string]$Url, [string]$Label) {
    try {
        $r = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 5
        Write-Host ("[OK] {0} -> {1} {2}" -f $Label, $r.StatusCode, $Url) -ForegroundColor Green
        return $true
    } catch {
        Write-Host ("[FAIL] {0} -> {1} ({2})" -f $Label, $Url, $_.Exception.Message) -ForegroundColor Red
        return $false
    }
}

Write-Host ""
Write-Host "== HTTP smoke ==" -ForegroundColor Cyan
$ok = $true
$ok = (Test-Url "http://localhost:9966/api/v1/health" "insight-server") -and $ok
$ok = (Test-Url "http://localhost:18091/hello" "provider") -and $ok
$ok = (Test-Url "http://localhost:18090/call" "consumer /call") -and $ok

if (-not $ok) {
    Write-Host "Some endpoints are down; start the three processes first." -ForegroundColor Yellow
    exit 2
}

Write-Host ""
Write-Host "[OK] All probes passed. Open http://localhost:9966/ for topology." -ForegroundColor Green
