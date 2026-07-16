Set-StrictMode -Version Latest

function Resolve-AgentProjectRoot {
    param(
        [string]$ProjectRoot,
        [string]$ScriptRoot
    )

    if ([string]::IsNullOrWhiteSpace($ProjectRoot)) {
        return (Resolve-Path -LiteralPath (Join-Path $ScriptRoot "..\..")).Path
    }

    return (Resolve-Path -LiteralPath $ProjectRoot).Path
}

function Initialize-AgentRunDirectory {
    param(
        [Parameter(Mandatory = $true)]
        [string]$ProjectRoot
    )

    $runDir = Join-Path $ProjectRoot ".run"
    if (-not (Test-Path -LiteralPath $runDir)) {
        New-Item -ItemType Directory -Path $runDir | Out-Null
    }
    return $runDir
}

function Get-AgentProcessCommandLine {
    param(
        [Parameter(Mandatory = $true)]
        [int]$ProcessId
    )

    $processInfo = Get-CimInstance Win32_Process -Filter "ProcessId = $ProcessId" -ErrorAction SilentlyContinue
    if ($null -eq $processInfo) {
        return ""
    }
    return [string]$processInfo.CommandLine
}

function Get-AgentProcessMatchText {
    param(
        [Parameter(Mandatory = $true)]
        [int]$ProcessId
    )

    $commandLine = Get-AgentProcessCommandLine -ProcessId $ProcessId
    $matchTextParts = @($commandLine)
    $argFileMatches = [regex]::Matches($commandLine, '@(?<path>[^\s"]+)')

    foreach ($match in $argFileMatches) {
        $argFilePath = $match.Groups["path"].Value
        if (-not $argFilePath.EndsWith(".argfile", [System.StringComparison]::OrdinalIgnoreCase)) {
            continue
        }
        if (-not (Test-Path -LiteralPath $argFilePath)) {
            continue
        }

        # Spring Boot 在 Windows 上可能把很长的 classpath 放入 @argfile。
        # 停止脚本需要读取该文件参与匹配，否则会把本项目控制面误判为非目标进程。
        $argFileContent = Get-Content -LiteralPath $argFilePath -Raw -Encoding UTF8
        $matchTextParts += $argFileContent
    }

    return ($matchTextParts -join " ")
}

function Format-AgentProcessPreview {
    param(
        [Parameter(Mandatory = $true)]
        [int]$ProcessId,
        [int]$MaxLength = 220
    )

    $process = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
    $processName = "<not-found>"
    if ($null -ne $process) {
        $processName = $process.ProcessName
    }

    $commandLine = Get-AgentProcessCommandLine -ProcessId $ProcessId
    if ([string]::IsNullOrWhiteSpace($commandLine)) {
        return "PID=$ProcessId Name=$processName CommandLine=<empty>"
    }

    # 警告里只展示命令行摘要，既能说明为什么跳过，也避免长启动参数刷屏。
    $normalizedCommandLine = $commandLine -replace "\s+", " "
    if ($normalizedCommandLine.Length -gt $MaxLength) {
        $normalizedCommandLine = $normalizedCommandLine.Substring(0, $MaxLength) + "..."
    }

    return "PID=$ProcessId Name=$processName CommandLine=$normalizedCommandLine"
}

function Test-AgentCommandLineMatch {
    param(
        [Parameter(Mandatory = $true)]
        [string]$CommandLine,
        [Parameter(Mandatory = $true)]
        [string[]]$RequiredTerms
    )

    foreach ($term in $RequiredTerms) {
        if ([string]::IsNullOrWhiteSpace($term)) {
            continue
        }
        if ($CommandLine -notlike "*$term*") {
            return $false
        }
    }
    return $true
}

function Get-AgentListeningProcessIds {
    param(
        [Parameter(Mandatory = $true)]
        [int]$Port
    )

    $connections = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    if ($null -eq $connections) {
        return @()
    }
    return @($connections | Select-Object -ExpandProperty OwningProcess -Unique)
}

function Test-AgentPortListening {
    param(
        [Parameter(Mandatory = $true)]
        [int]$Port
    )

    return (@(Get-AgentListeningProcessIds -Port $Port).Count -gt 0)
}

function Stop-AgentProcessTree {
    param(
        [Parameter(Mandatory = $true)]
        [int]$ProcessId,
        [switch]$DryRun
    )

    $children = @(Get-CimInstance Win32_Process -Filter "ParentProcessId = $ProcessId" -ErrorAction SilentlyContinue)
    foreach ($child in $children) {
        Stop-AgentProcessTree -ProcessId ([int]$child.ProcessId) -DryRun:$DryRun
    }

    $process = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
    if ($null -eq $process) {
        return
    }

    if ($DryRun) {
        Write-Host "[DRY-RUN] 将停止进程：PID=$ProcessId Name=$($process.ProcessName)"
        return
    }

    # 停止进程树时，Maven/cmd 子进程可能已经随 Java 主进程自然退出。
    # 这里把“刚好已经退出”视为幂等成功，避免给使用者展示无害但刺眼的错误。
    Stop-Process -Id $ProcessId -Force -ErrorAction SilentlyContinue
}

function Stop-AgentManagedProcess {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name,
        [Parameter(Mandatory = $true)]
        [string]$PidFile,
        [Parameter(Mandatory = $true)]
        [int]$Port,
        [Parameter(Mandatory = $true)]
        [string[]]$RequiredTerms,
        [switch]$DryRun
    )

    $stopped = $false

    if (Test-Path -LiteralPath $PidFile) {
        $pidText = (Get-Content -LiteralPath $PidFile -Raw -Encoding UTF8).Trim()
        $managedPid = 0
        if ([int]::TryParse($pidText, [ref]$managedPid)) {
            $managedProcess = Get-Process -Id $managedPid -ErrorAction SilentlyContinue
            if ($null -ne $managedProcess) {
                $matchText = Get-AgentProcessMatchText -ProcessId $managedPid
                if (Test-AgentCommandLineMatch -CommandLine $matchText -RequiredTerms $RequiredTerms) {
                    Stop-AgentProcessTree -ProcessId $managedPid -DryRun:$DryRun
                    $stopped = $true
                }
                else {
                    $preview = Format-AgentProcessPreview -ProcessId $managedPid
                    Write-Warning "$Name 的 PID 文件指向的进程命令行特征不匹配，已跳过，避免误停无关进程。$preview"
                }
            }
        }

        if (-not $DryRun -and $stopped) {
            Remove-Item -LiteralPath $PidFile -Force
        }
        elseif (-not $DryRun -and -not $stopped -and $managedPid -eq 0) {
            Remove-Item -LiteralPath $PidFile -Force
        }
    }

    if ($stopped) {
        if ($DryRun) {
            Write-Host "$Name dry-run 检查完成，未实际停止进程。"
        }
        else {
            Write-Host "$Name 已停止。"
        }
        return
    }

    $candidatePids = @(Get-AgentListeningProcessIds -Port $Port)
    foreach ($candidatePid in $candidatePids) {
        $matchText = Get-AgentProcessMatchText -ProcessId ([int]$candidatePid)
        if (Test-AgentCommandLineMatch -CommandLine $matchText -RequiredTerms $RequiredTerms) {
            Stop-AgentProcessTree -ProcessId ([int]$candidatePid) -DryRun:$DryRun
            $stopped = $true
        }
        else {
            $preview = Format-AgentProcessPreview -ProcessId ([int]$candidatePid)
            Write-Warning "端口 $Port 上存在非本项目目标进程，命令行特征不匹配，已跳过。$preview"
        }
    }

    if ($stopped) {
        if (-not $DryRun -and (Test-Path -LiteralPath $PidFile)) {
            Remove-Item -LiteralPath $PidFile -Force
        }
        if ($DryRun) {
            Write-Host "$Name dry-run 检查完成，未实际停止进程。"
        }
        else {
            Write-Host "$Name 已停止。"
        }
        return
    }

    Write-Host "未发现需要停止的 $Name。"
}
