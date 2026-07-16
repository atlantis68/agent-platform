[CmdletBinding()]
param(
    [string]$ProjectRoot,
    [string]$HostName = "127.0.0.1",
    [int]$Port = 8001,
    [switch]$InstallDependencies,
    [switch]$CheckOnly
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
. "$PSScriptRoot\_process-utils.ps1"

$projectRootPath = Resolve-AgentProjectRoot -ProjectRoot $ProjectRoot -ScriptRoot $PSScriptRoot
$runDir = Initialize-AgentRunDirectory -ProjectRoot $projectRootPath
$pidFile = Join-Path $runDir "python-runtime.pid"
$stdoutLog = Join-Path $runDir "python-runtime.out.log"
$stderrLog = Join-Path $runDir "python-runtime.err.log"
$pythonExe = Join-Path $projectRootPath "runtime\.venv\Scripts\python.exe"

if (-not (Test-Path -LiteralPath $pythonExe)) {
    if (-not $InstallDependencies) {
        throw "未找到 Runtime 虚拟环境：$pythonExe。请先运行 python -m venv runtime\.venv，或使用 -InstallDependencies。"
    }

    if ($CheckOnly) {
        Write-Host "[CHECK] 将创建 Runtime 虚拟环境并安装依赖。"
    }
    else {
        python -m venv (Join-Path $projectRootPath "runtime\.venv")
        & $pythonExe -m pip install -r (Join-Path $projectRootPath "runtime\requirements-dev.txt")
    }
}

if (Test-AgentPortListening -Port $Port) {
    throw "端口 $Port 已被监听。请先确认是否已有 Python Runtime 正在运行，或改用 -Port 指定新端口。"
}

$arguments = @(
    "-m",
    "uvicorn",
    "app.main:app",
    "--app-dir",
    "runtime",
    "--host",
    $HostName,
    "--port",
    "$Port"
)

if ($CheckOnly) {
    Write-Host "[CHECK] Python Runtime 启动命令："
    Write-Host "$pythonExe $($arguments -join ' ')"
    Write-Host "[CHECK] 日志目录：$runDir"
    return
}

$process = Start-Process `
    -FilePath $pythonExe `
    -ArgumentList $arguments `
    -WorkingDirectory $projectRootPath `
    -WindowStyle Hidden `
    -RedirectStandardOutput $stdoutLog `
    -RedirectStandardError $stderrLog `
    -PassThru

Set-Content -LiteralPath $pidFile -Value $process.Id -Encoding UTF8
Write-Host "Python Runtime 已启动：PID=$($process.Id)，地址=http://$HostName`:$Port"
Write-Host "日志：$stdoutLog / $stderrLog"
