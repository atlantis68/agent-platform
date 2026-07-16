[CmdletBinding()]
param(
    [string]$ProjectRoot,
    [int]$Port = 8002,
    [string]$RuntimeBaseUrl = "http://127.0.0.1:8001",
    [string]$MavenCommand = "mvn",
    [switch]$CheckOnly
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
. "$PSScriptRoot\_process-utils.ps1"

$projectRootPath = Resolve-AgentProjectRoot -ProjectRoot $ProjectRoot -ScriptRoot $PSScriptRoot
$runDir = Initialize-AgentRunDirectory -ProjectRoot $projectRootPath
$pidFile = Join-Path $runDir "java-control-plane.pid"
$stdoutLog = Join-Path $runDir "java-control-plane.out.log"
$stderrLog = Join-Path $runDir "java-control-plane.err.log"
$pomPath = Join-Path $projectRootPath "control-plane\pom.xml"
$maven = Get-Command $MavenCommand -ErrorAction Stop

if (-not (Test-Path -LiteralPath $pomPath)) {
    throw "未找到控制面 Maven 工程：$pomPath"
}

if (Test-AgentPortListening -Port $Port) {
    throw "端口 $Port 已被监听。请先确认是否已有 Java 控制面正在运行，或改用 -Port 指定新端口。"
}

$runtimeUri = [System.Uri]$RuntimeBaseUrl
if ($runtimeUri.IsLoopback -and -not (Test-AgentPortListening -Port $runtimeUri.Port)) {
    Write-Warning "未检测到本地 Runtime 端口 $($runtimeUri.Port) 正在监听。控制面可以启动，但创建 Agent Run 时会依赖该 Runtime。"
}

$arguments = @(
    "-f",
    "control-plane/pom.xml",
    "spring-boot:run"
)

if ($CheckOnly) {
    Write-Host "[CHECK] Java 控制面启动命令："
    Write-Host "$($maven.Source) $($arguments -join ' ')"
    Write-Host "[CHECK] 环境变量：SERVER_PORT=$Port"
    Write-Host "[CHECK] 环境变量：AGENT_RUNTIME_BASE_URL=$RuntimeBaseUrl"
    Write-Host "[CHECK] 日志目录：$runDir"
    return
}

$previousServerPort = $env:SERVER_PORT
$previousRuntimeBaseUrl = $env:AGENT_RUNTIME_BASE_URL

try {
    # 通过环境变量传递 Spring Boot 配置，避免 Windows Start-Process / Maven / Spring Boot
    # 三层命令行解析对带空格应用参数的处理差异。Spring Boot relaxed binding 会把
    # SERVER_PORT 绑定到 server.port，把 AGENT_RUNTIME_BASE_URL 绑定到 agent.runtime.base-url。
    $env:SERVER_PORT = "$Port"
    $env:AGENT_RUNTIME_BASE_URL = $RuntimeBaseUrl

    $process = Start-Process `
        -FilePath $maven.Source `
        -ArgumentList $arguments `
        -WorkingDirectory $projectRootPath `
        -WindowStyle Hidden `
        -RedirectStandardOutput $stdoutLog `
        -RedirectStandardError $stderrLog `
        -PassThru
}
finally {
    if ($null -eq $previousServerPort) {
        Remove-Item Env:\SERVER_PORT -ErrorAction SilentlyContinue
    }
    else {
        $env:SERVER_PORT = $previousServerPort
    }

    if ($null -eq $previousRuntimeBaseUrl) {
        Remove-Item Env:\AGENT_RUNTIME_BASE_URL -ErrorAction SilentlyContinue
    }
    else {
        $env:AGENT_RUNTIME_BASE_URL = $previousRuntimeBaseUrl
    }
}

Set-Content -LiteralPath $pidFile -Value $process.Id -Encoding UTF8
Write-Host "Java 控制面已启动：PID=$($process.Id)，地址=http://127.0.0.1`:$Port/index.html"
Write-Host "日志：$stdoutLog / $stderrLog"
