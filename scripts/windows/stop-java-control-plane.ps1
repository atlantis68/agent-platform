[CmdletBinding()]
param(
    [string]$ProjectRoot,
    [int]$Port = 8002,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
. "$PSScriptRoot\_process-utils.ps1"

$projectRootPath = Resolve-AgentProjectRoot -ProjectRoot $ProjectRoot -ScriptRoot $PSScriptRoot
$runDir = Initialize-AgentRunDirectory -ProjectRoot $projectRootPath
$pidFile = Join-Path $runDir "java-control-plane.pid"

# Java 控制面由 Maven 启动，停止时递归停止进程树，确保 Maven 子进程和应用进程不会残留。
Stop-AgentManagedProcess `
    -Name "Java 控制面" `
    -PidFile $pidFile `
    -Port $Port `
    -RequiredTerms @("control-plane") `
    -DryRun:$DryRun
