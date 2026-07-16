[CmdletBinding()]
param(
    [string]$ProjectRoot,
    [int]$Port = 8001,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
. "$PSScriptRoot\_process-utils.ps1"

$projectRootPath = Resolve-AgentProjectRoot -ProjectRoot $ProjectRoot -ScriptRoot $PSScriptRoot
$runDir = Initialize-AgentRunDirectory -ProjectRoot $projectRootPath
$pidFile = Join-Path $runDir "python-runtime.pid"

# 停止脚本同时校验端口和命令行特征，避免把 8001 上的其他本地服务误停。
Stop-AgentManagedProcess `
    -Name "Python Runtime" `
    -PidFile $pidFile `
    -Port $Port `
    -RequiredTerms @("uvicorn", "app.main:app", "$Port") `
    -DryRun:$DryRun
