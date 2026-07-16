# Agent 平台阶段 1 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:test-driven-development、agent-platform-project-governance 和 superpowers:verification-before-completion。步骤使用 Markdown 复选框语法来跟踪进度。项目规则禁止 Git 写操作，因此本计划不包含 commit 步骤。

**目标：** 交付一个可本机运行的最小 Agent Runtime 闭环：Java 控制面创建 Agent Run，Python Runtime 执行本地开发模型，控制面通过 SSE 返回事件，并保存短期记忆与基础审计。

**架构：** Java/Spring Boot 作为控制面，负责公开 API、静态运行台、运行记录、审计事件和 SSE 代理；Python/FastAPI 作为 Runtime，负责执行 Agent Run、维护会话级短期记忆、生成本地开发模型响应和 Runtime 事件。阶段 1 只做单 Agent、单 Runtime、内存存储和本机演示，不实现工具/MCP、RAG、长期记忆、Skill 运行时或 CLI。

**技术栈：** Java 17、Maven 3.9.11、Spring Boot 3.5.x、Python 3.12、FastAPI、pytest、SSE、内存存储。

---

## 文件结构

| 文件或目录 | 职责 |
|---|---|
| `control-plane/` | Java 控制面应用，提供公开 API、SSE、静态运行台和审计查询 |
| `runtime/` | Python Runtime，提供 `/internal/v1/runs`、运行事件、短期记忆 |
| `docs/delivery/phase-1-showcase.md` | 阶段 1 成果展示、启动方式、验收标准和限制 |
| `docs/superpowers/plans/2026-07-15-agent-platform-phase1.md` | 本实施计划 |
| `progress.md` | 阶段 1 执行进度 |
| `.agents/skills/agent-platform-project-governance/references/problem-log.md` | 问题、根因、修复和预防记录 |

### 任务 1：Python Runtime 红绿闭环

**文件：**
- 创建：`runtime/requirements.txt`
- 创建：`runtime/requirements-dev.txt`
- 创建：`runtime/app/__init__.py`
- 创建：`runtime/app/main.py`
- 创建：`runtime/tests/test_runtime.py`

- [x] **步骤 1：编写失败测试**

测试应验证：

```python
def test_run_records_events_and_short_term_memory(client):
    payload = build_runtime_run(run_id="run_001", conversation_id="conv_001", text="第一轮")
    response = client.post("/internal/v1/runs", json=payload)
    assert response.status_code == 200
    events = client.get("/internal/v1/runs/run_001/events").json()["events"]
    assert [event["type"] for event in events] == [
        "run.started",
        "model.requested",
        "run.output.delta",
        "model.completed",
        "run.completed",
    ]
    memory = client.get("/internal/v1/conversations/conv_001/messages").json()["messages"]
    assert [message["role"] for message in memory] == ["user", "assistant"]
```

- [x] **步骤 2：运行测试验证失败**

运行：

```powershell
python -m pytest runtime/tests -q
```

预期：失败，原因是 `runtime/app/main.py` 或接口尚未实现。

- [x] **步骤 3：实现 Runtime 最小代码**

实现内容：

- FastAPI 应用。
- `/internal/v1/runs` 接收阶段 0 协议字段。
- 本地开发模型生成确定性回复。
- 内存保存 `RunEvent` 和 conversation messages。
- `/internal/v1/runs/{runId}/events` 返回事件列表。
- `/internal/v1/conversations/{conversationId}/messages` 返回短期记忆。
- `/health` 返回 Runtime 健康状态。

- [x] **步骤 4：运行测试验证通过**

运行：

```powershell
python -m pytest runtime/tests -q
```

预期：全部通过。

### 任务 2：Java 控制面红绿闭环

**文件：**
- 创建：`control-plane/pom.xml`
- 创建：`control-plane/src/main/java/com/agentplatform/control/ControlPlaneApplication.java`
- 创建：`control-plane/src/main/java/com/agentplatform/control/agent/*.java`
- 创建：`control-plane/src/main/resources/application.yml`
- 创建：`control-plane/src/main/resources/static/index.html`
- 创建：`control-plane/src/test/java/com/agentplatform/control/agent/AgentRunControllerTest.java`

- [x] **步骤 1：编写失败测试**

测试应验证：

```java
mockMvc.perform(post("/api/v1/agent-runs")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"agentId":"agent_general_001","input":{"type":"text","text":"你好"},"context":{"conversationId":"conv_001"}}
            """))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.runId").exists())
    .andExpect(jsonPath("$.status").value("queued"))
    .andExpect(jsonPath("$.eventStreamUrl").exists());
```

- [x] **步骤 2：运行测试验证失败**

运行：

```powershell
mvn -f control-plane/pom.xml test
```

预期：失败，原因是控制面类和 API 尚未实现。

- [x] **步骤 3：实现控制面最小代码**

实现内容：

- `POST /api/v1/agent-runs` 创建运行并调用 Runtime。
- `GET /api/v1/agent-runs/{runId}` 查询运行摘要。
- `GET /api/v1/agent-runs/{runId}/events` 返回 SSE。
- `GET /api/v1/agent-runs/{runId}/audit-events` 返回审计事件。
- 静态 `index.html` 提供阶段 1 运行台。
- `RuntimeClient` 使用 HTTP 调用 Python Runtime。

- [x] **步骤 4：运行测试验证通过**

运行：

```powershell
mvn -f control-plane/pom.xml test
```

预期：全部通过。

### 任务 3：本机联调与阶段展示

**文件：**
- 创建：`docs/delivery/phase-1-showcase.md`
- 修改：`README.md`
- 修改：`progress.md`
- 修改：`task_plan.md`

- [x] **步骤 1：启动 Python Runtime**

运行：

```powershell
python -m uvicorn app.main:app --app-dir runtime --host 127.0.0.1 --port 8001
```

预期：`/health` 返回 `{"status":"ok"}`。

- [x] **步骤 2：启动 Java 控制面**

运行：

```powershell
mvn -f control-plane/pom.xml spring-boot:run
```

预期：`http://127.0.0.1:8080` 可打开阶段 1 运行台。

- [x] **步骤 3：执行端到端验收**

运行：

```powershell
$body = '{"agentId":"agent_general_001","input":{"type":"text","text":"阶段1验收"},"context":{"conversationId":"conv_acceptance"}}'
Invoke-RestMethod -Uri 'http://127.0.0.1:8080/api/v1/agent-runs' -Method Post -ContentType 'application/json' -Body $body
```

预期：

- 返回 `runId`、`traceId`、`status=queued` 和 `eventStreamUrl`。
- 事件流包含 `run.started`、`model.requested`、`run.output.delta`、`model.completed`、`run.completed`。
- 审计接口能查询到事件。

- [x] **步骤 4：归档阶段 1 展示**

写入 `docs/delivery/phase-1-showcase.md`，包含：

- 为什么阶段 1 使用本地开发模型。
- 如何启动 Runtime 和控制面。
- 如何打开运行台。
- 验收命令和结果。
- 已知限制：无工具/MCP、无 RAG、无长期记忆、无 CLI、无真实外部模型供应商。

## 自检结果

| 检查项 | 结果 |
|---|---|
| 阶段边界 | pass，仅覆盖阶段 1 |
| 不越阶段 | pass，不实现工具/MCP、RAG、长期记忆、Skill 运行时、CLI |
| 本机约束 | pass，不包含 Git 写操作 |
| TDD 路径 | pass，Runtime 和控制面均先写测试再实现 |
| 展示交付 | pass，包含运行台、验收命令和展示文档 |

## 实际验证结果

| 验证项 | 结果 |
|---|---|
| Runtime 测试 | `runtime\.venv\Scripts\python -m pytest runtime\tests -q`，`2 passed` |
| Runtime 依赖 | `runtime\.venv\Scripts\python -m pip check`，`No broken requirements found.` |
| 控制面测试 | `mvn -f control-plane/pom.xml test`，`Tests run: 5, Failures: 0, Errors: 0, Skipped: 0` |
| 本机端到端 | 运行台 HTTP 200；创建运行 `queued`；摘要 `completed`；审计事件 5 条；SSE 包含 `run.completed`；短期记忆为 `user,assistant` |
