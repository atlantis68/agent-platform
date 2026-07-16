# 阶段 1 成果展示：最小 Agent Runtime

## 阶段目标

交付一个可在本机运行的最小 Agent Runtime 闭环：Java 控制面创建 Agent Run，Python Runtime 执行本地开发模型，控制面返回 SSE 事件，并提供短期记忆与基础审计能力。

## 为什么使用本地开发模型

阶段 1 的目标是验证平台主链路，而不是验证外部模型供应商能力。本地确定性模型可以避免 API Key、网络、额度和模型波动影响验收，让控制面、Runtime 协议、事件顺序、短期记忆和审计查询先稳定下来。

## 本阶段已交付

| 交付物 | 路径 | 用途 |
|---|---|---|
| Python Runtime | `runtime/app/main.py` | 提供 `/internal/v1/runs`、事件查询、会话短期记忆和健康检查 |
| Runtime 测试 | `runtime/tests/test_runtime.py` | 验证事件顺序、trace/run 标识、短期记忆窗口 |
| Runtime 依赖 | `runtime/requirements.txt`、`runtime/requirements-dev.txt` | 固定阶段 1 已验证依赖区间 |
| Java 控制面 | `control-plane/src/main/java/com/agentplatform/control` | 提供 Agent Run API、SSE、审计查询和静态运行台；`agent` 下按 `controller/service/client/dto/entity` 分层 |
| 控制面测试 | `control-plane/src/test/java/com/agentplatform/control` | 验证控制器、HTTP Runtime 适配器和真实 Spring Bean 装配 |
| 阶段 1 运行台 | `control-plane/src/main/resources/static/index.html` | 本机展示创建运行和接收事件 |
| 阶段 1 实施计划 | `docs/superpowers/plans/2026-07-15-agent-platform-phase1.md` | 记录阶段范围、任务和验收路径 |
| 项目问题日志 | `.agents/skills/agent-platform-project-governance/references/problem-log.md` | 记录阶段 1 依赖、HTTP、命令和装配问题 |
| Windows 本机启停脚本 | `scripts/windows/*.ps1` | 简化 Python Runtime 和 Java 控制面的本机启停、日志和 PID 管理 |

## 用户验收结论

用户已在 2026-07-16 完成阶段 1 改造验收，结论为通过。本阶段最终状态如下：

- Agent Run 创建、Runtime 执行、本地开发模型输出、短期记忆、审计事件、SSE 事件流均可用。
- 控制面 `agent` 包已按 `client/controller/dto/entity/service` 分层。
- Python Runtime 已按 `controller/dto/entity/service/utils` 分层。
- 所有代码备注统一中文的规则已写入 `AGENTS.md`、`task_plan.md` 和项目专属 Skill。
- 浏览器直接访问 SSE `/events` 中文乱码问题已修复，响应头明确为 `text/event-stream;charset=UTF-8`。

## 启动方式

首次运行 Runtime 前安装依赖：

```powershell
python -m venv runtime\.venv
runtime\.venv\Scripts\python -m pip install -r runtime\requirements-dev.txt
```

启动 Python Runtime：

```powershell
runtime\.venv\Scripts\python -m uvicorn app.main:app --app-dir runtime --host 127.0.0.1 --port 8001
```

启动 Java 控制面：

```powershell
mvn -f control-plane/pom.xml spring-boot:run
```

也可以使用 Windows 本机启停脚本：

```powershell
.\scripts\windows\start-python-runtime.ps1
.\scripts\windows\start-java-control-plane.ps1
```

停止本机服务：

```powershell
.\scripts\windows\stop-java-control-plane.ps1
.\scripts\windows\stop-python-runtime.ps1
```

运行台地址：

```text
http://127.0.0.1:8080/index.html
```

## 验收命令与结果

| 验收项 | 命令 | 结果 |
|---|---|---|
| Runtime 依赖一致性 | `runtime\.venv\Scripts\python -m pip check` | pass，`No broken requirements found.` |
| Runtime 自动化测试 | `runtime\.venv\Scripts\python -m pytest runtime\tests -q` | pass，`2 passed` |
| 控制面自动化测试 | `mvn -f control-plane\pom.xml clean test` | pass，`Tests run: 6, Failures: 0, Errors: 0, Skipped: 0` |
| Runtime 健康检查 | `GET http://127.0.0.1:8001/health` | pass，`status=ok` |
| 运行台页面 | `GET http://127.0.0.1:8080/index.html` | pass，HTTP 200 |
| 端到端 Agent Run | 控制面创建运行，查询审计、SSE 和 Runtime 短期记忆 | pass，见下方结果 |
| SSE 中文输出 | 访问控制面 `/api/v1/agent-runs/{runId}/events` | pass，`Content-Type=text/event-stream;charset=UTF-8`，中文 payload 无替换字符 |

端到端验收结果：

```json
{
  "runId": "run_54eb242d7aa545be8cf0c4ea199afcdd",
  "traceId": "tr_f9d1b3eb7e184d24a0ecb009896fbb51",
  "createStatus": "queued",
  "summaryStatus": "completed",
  "eventCount": 5,
  "eventTypes": [
    "run.started",
    "model.requested",
    "run.output.delta",
    "model.completed",
    "run.completed"
  ],
  "sseContainsCompleted": true,
  "memoryRoles": [
    "user",
    "assistant"
  ],
  "toolCalls": 0
}
```

## 已知限制

| 限制 | 后续阶段 |
|---|---|
| 运行状态、审计事件和短期记忆仍为内存存储 | 阶段 7 引入持久化工作流前逐步替换 |
| 本地开发模型只返回确定性文本，不连接外部模型供应商 | 后续模型网关阶段 |
| 未开放工具、MCP、RAG、长期记忆、Skill 运行时和 CLI | 分别在阶段 2-6 落地 |
| SSE 当前一次性返回 Runtime 已产生事件，不做长任务持续推送 | 阶段 7 持久化任务和可靠执行扩展 |
| 控制面只内置一个阶段 1 默认 Agent Snapshot | 后续 Agent 配置与版本管理阶段 |

## 下一阶段入口条件

阶段 2 可以在用户明确要求后开始，入口条件如下：

1. 保持阶段 1 的 Agent Run、RunEvent、SSE 和审计 API 不破坏。
2. Tool Registry 必须先定义工具元数据、输入 schema、风险等级和审计策略。
3. MCP v1 只接入 tools，不提前扩展 RAG、长期记忆、Skill 运行时或 CLI。
4. 所有工具调用必须产生审计事件，并默认拒绝高风险写操作。

阶段 2 具体实施计划见：`docs/superpowers/plans/2026-07-16-agent-platform-phase2-tool-mcp.md`。
