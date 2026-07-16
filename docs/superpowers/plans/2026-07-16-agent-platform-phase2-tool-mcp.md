# 阶段 2 工具系统与 MCP v1 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。项目规则覆盖默认计划模板：本项目暂不执行 Git 提交，所有代码备注必须使用中文。

**目标：** 在不破坏阶段 1 Agent Run 闭环的前提下，让 Agent 能调用一个低风险 HTTP 工具和一个 MCP tool，并在控制面展示完整审计事件。

**架构：** Java 控制面继续负责 Tool Registry、工具元数据展示、运行摘要和审计查询；Python Runtime 负责在本地执行工具调用、生成工具事件并返回给控制面。阶段 2 只接入 MCP tools，不接入 MCP resources/prompts，也不提前实现 RAG、长期记忆、Skill 运行时或 CLI。

**技术栈：** Java 17、Spring Boot 3、Python 3.12、FastAPI、pytest、Maven、MCP tools 协议、JSON Schema。

---

## 需要创建或修改的文件

| 文件 | 动作 | 职责 |
|---|---|---|
| `control-plane/src/main/java/com/agentplatform/control/agent/dto/ToolDefinition.java` | 创建 | 控制面对外展示工具元数据 |
| `control-plane/src/main/java/com/agentplatform/control/agent/dto/ToolRiskLevel.java` | 创建 | 定义 `LOW/MEDIUM/HIGH` 风险等级 |
| `control-plane/src/main/java/com/agentplatform/control/agent/controller/ToolRegistryController.java` | 创建 | 提供 `GET /api/v1/tools` 工具列表 |
| `control-plane/src/main/java/com/agentplatform/control/agent/service/ToolRegistryService.java` | 创建 | 维护阶段 2 内存工具注册表 |
| `control-plane/src/main/java/com/agentplatform/control/agent/service/AgentRunService.java` | 修改 | 默认 Agent Snapshot 启用阶段 2 低风险工具 |
| `control-plane/src/main/java/com/agentplatform/control/agent/entity/StoredRun.java` | 修改 | 统计 `tool.completed` 事件，更新 `toolCalls` |
| `control-plane/src/test/java/com/agentplatform/control/agent/controller/ToolRegistryControllerTest.java` | 创建 | 验证工具列表、风险等级和 schema |
| `control-plane/src/test/java/com/agentplatform/control/agent/controller/AgentRunControllerTest.java` | 修改 | 验证审计事件包含工具调用且 SSE 可读 |
| `runtime/app/dto/runtime.py` | 修改 | 明确 Runtime 请求中的 `enabledTools` 语义 |
| `runtime/app/entity/tool.py` | 创建 | Runtime 内部工具定义、工具结果实体 |
| `runtime/app/service/tool_registry.py` | 创建 | Runtime 本地工具注册表 |
| `runtime/app/service/tool_executor.py` | 创建 | 执行低风险 HTTP demo 工具 |
| `runtime/app/service/mcp_tool_client.py` | 创建 | 阶段 2 MCP tool 调用适配层 |
| `runtime/app/service/runtime_service.py` | 修改 | 在 Agent Run 中按输入触发工具调用并追加审计事件 |
| `runtime/tests/test_runtime.py` | 修改 | 增加 HTTP 工具、MCP tool 和失败路径测试 |
| `docs/delivery/phase-2-showcase.md` | 创建 | 阶段 2 成果展示、验收命令和限制 |
| `.agents/skills/agent-platform-project-governance/references/problem-log.md` | 修改 | 记录阶段 2 过程中出现的问题和修复 |

## 阶段 2 范围

**本阶段要做：**

1. Tool Registry：工具名称、描述、输入 schema、风险等级、来源类型。
2. HTTP 工具：默认实现一个低风险 `http_echo` demo 工具，用于验证 HTTP 工具调用链路。
3. MCP tool：默认实现一个低风险 `mcp_local_time` demo tool，用于验证 MCP tool 调用抽象和审计链路。
4. 审计事件：每次工具调用必须产生 `tool.requested`、`tool.completed` 或 `tool.failed`。
5. 运行摘要：`RunUsage.toolCalls` 从工具完成事件中派生。
6. 控制面展示：工具列表接口、审计接口和 SSE 都能看到工具调用。

**本阶段不做：**

1. 不接入 MCP resources 和 MCP prompts。
2. 不实现 RAG、长期记忆、Skill 运行时、CLI 沙箱。
3. 不开放真实高风险写操作。
4. 不引入数据库持久化；仍沿用阶段 1 内存存储。

## 默认演示工具

| 工具名 | 类型 | 风险 | 输入 | 输出 | 触发方式 |
|---|---|---|---|---|---|
| `http_echo` | HTTP | LOW | `{ "text": "..." }` | 回显文本和长度 | 用户输入包含“echo”或“回显” |
| `mcp_local_time` | MCP tool | LOW | `{ "timezone": "Asia/Shanghai" }` | 本地时间字符串 | 用户输入包含“mcp 时间”或“当前时间” |

如果用户后续提供真实 HTTP API 或真实 MCP server 配置，上表可替换为真实工具；替换前必须先更新测试和风险等级。

## 启动默认策略

阶段 2 可以在没有真实外部工具配置的情况下启动。默认策略如下：

1. 首批 HTTP 工具使用 `http_echo`，只验证平台工具调用协议、输入 schema、事件和审计链路。
2. 首批 MCP tool 使用 `mcp_local_time` 本地适配层，先固定 MCP tool 调用抽象，暂不扩大到 MCP resources 或 prompts。
3. 工具风险等级默认只允许 `LOW`，写操作、外部系统变更和高风险命令全部拒绝。
4. 默认不新增真实 MCP SDK 依赖；如需直接接入真实 MCP server，先由用户确认 SDK、transport、启动命令和鉴权方式。

这样做的原因是阶段 2 的首要目标是验证工具调用主链路，而不是把真实业务系统、外部凭据和治理审批一次性引入。真实工具接入可以在 demo 闭环通过后替换，替换前必须先更新测试和风险策略。

## 任务 1：控制面 Tool Registry 红灯测试

**文件：**

- 创建：`control-plane/src/test/java/com/agentplatform/control/agent/controller/ToolRegistryControllerTest.java`
- 创建：`control-plane/src/main/java/com/agentplatform/control/agent/controller/ToolRegistryController.java`
- 创建：`control-plane/src/main/java/com/agentplatform/control/agent/service/ToolRegistryService.java`
- 创建：`control-plane/src/main/java/com/agentplatform/control/agent/dto/ToolDefinition.java`
- 创建：`control-plane/src/main/java/com/agentplatform/control/agent/dto/ToolRiskLevel.java`

- [ ] **步骤 1：编写失败测试**

```java
@Test
void listToolsReturnsPhaseTwoDemoTools() throws Exception {
    mockMvc.perform(get("/api/v1/tools"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tools[0].name").value("http_echo"))
            .andExpect(jsonPath("$.tools[0].riskLevel").value("LOW"))
            .andExpect(jsonPath("$.tools[0].inputSchema.type").value("object"))
            .andExpect(jsonPath("$.tools[1].name").value("mcp_local_time"))
            .andExpect(jsonPath("$.tools[1].sourceType").value("mcp"));
}
```

- [ ] **步骤 2：运行测试确认失败**

运行：

```powershell
mvn -f control-plane\pom.xml "-Dtest=com.agentplatform.control.agent.controller.ToolRegistryControllerTest" test
```

预期：失败，原因是 `ToolRegistryControllerTest` 或 `/api/v1/tools` 尚不存在。

- [ ] **步骤 3：实现最小控制面工具注册表**

实现要点：

```java
public record ToolDefinition(
        String name,
        String displayName,
        String description,
        String sourceType,
        ToolRiskLevel riskLevel,
        Map<String, Object> inputSchema
) {
}
```

`ToolRegistryService` 返回 `http_echo` 和 `mcp_local_time` 两个内存工具定义。所有 JavaDoc 和注释必须使用中文。

- [ ] **步骤 4：运行测试确认通过**

运行：

```powershell
mvn -f control-plane\pom.xml "-Dtest=com.agentplatform.control.agent.controller.ToolRegistryControllerTest" test
```

预期：`Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`。

## 任务 2：Runtime 工具事件红灯测试

**文件：**

- 修改：`runtime/tests/test_runtime.py`
- 创建：`runtime/app/entity/tool.py`
- 创建：`runtime/app/service/tool_registry.py`
- 创建：`runtime/app/service/tool_executor.py`
- 创建：`runtime/app/service/mcp_tool_client.py`
- 修改：`runtime/app/service/runtime_service.py`

- [ ] **步骤 1：编写 HTTP 工具调用失败测试**

```python
def test_run_can_call_low_risk_http_tool():
    client = TestClient(app)
    payload = build_runtime_run("run_http_tool", "conv_tools", "请 echo 阶段 2 工具")

    assert client.post("/internal/v1/runs", json=payload).status_code == 200

    events = client.get("/internal/v1/runs/run_http_tool/events").json()["events"]
    assert [event["type"] for event in events] == [
        "run.started",
        "model.requested",
        "tool.requested",
        "tool.completed",
        "run.output.delta",
        "model.completed",
        "run.completed",
    ]
    tool_event = next(event for event in events if event["type"] == "tool.completed")
    assert tool_event["payload"]["toolName"] == "http_echo"
    assert tool_event["payload"]["riskLevel"] == "LOW"
```

- [ ] **步骤 2：编写 MCP tool 失败测试**

```python
def test_run_can_call_low_risk_mcp_tool():
    client = TestClient(app)
    payload = build_runtime_run("run_mcp_tool", "conv_tools_mcp", "请查询 mcp 时间")

    assert client.post("/internal/v1/runs", json=payload).status_code == 200

    events = client.get("/internal/v1/runs/run_mcp_tool/events").json()["events"]
    tool_event = next(event for event in events if event["type"] == "tool.completed")
    assert tool_event["payload"]["toolName"] == "mcp_local_time"
    assert tool_event["payload"]["sourceType"] == "mcp"
```

- [ ] **步骤 3：运行测试确认失败**

运行：

```powershell
runtime\.venv\Scripts\python -m pytest runtime\tests -q
```

预期：新增测试失败，原因是 Runtime 尚未产生工具事件。

- [ ] **步骤 4：实现 Runtime 工具注册与执行**

实现要点：

```python
class ToolDefinition(BaseModel):
    """Runtime 内部使用的工具定义。"""

    name: str
    source_type: Literal["http", "mcp"]
    risk_level: Literal["LOW", "MEDIUM", "HIGH"]
    input_schema: dict[str, Any]
```

`tool_executor.py` 只允许执行 `LOW` 风险工具。`mcp_tool_client.py` 先提供 `mcp_local_time` 的本地适配实现，后续替换为真实 MCP SDK client 时保持返回结构不变。

- [ ] **步骤 5：运行测试确认通过**

运行：

```powershell
runtime\.venv\Scripts\python -m pytest runtime\tests -q
```

预期：Runtime 测试全部通过。

## 任务 3：控制面运行摘要和审计展示

**文件：**

- 修改：`control-plane/src/main/java/com/agentplatform/control/agent/entity/StoredRun.java`
- 修改：`control-plane/src/test/java/com/agentplatform/control/agent/controller/AgentRunControllerTest.java`
- 修改：`control-plane/src/main/resources/static/index.html`

- [ ] **步骤 1：编写失败测试**

```java
@Test
void runSummaryCountsCompletedToolEvents() throws Exception {
    when(runtimeClient.submitRun(any(RuntimeRunRequest.class)))
            .thenReturn(new RuntimeAcceptedResponse("run_mocked", "accepted"));
    when(runtimeClient.fetchEvents(any(String.class)))
            .thenReturn(List.of(
                    new RunEvent("evt_1", "tr_mocked", "run_mocked", "tool.completed", 1,
                            "2026-07-16T09:00:00Z", Map.of("toolName", "http_echo")),
                    new RunEvent("evt_2", "tr_mocked", "run_mocked", "run.completed", 2,
                            "2026-07-16T09:00:01Z", Map.of("status", "completed"))
            ));

    String response = createRunAndReturnBody("工具统计");
    String runId = extractRunId(response);

    mockMvc.perform(get("/api/v1/agent-runs/" + runId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.usage.toolCalls").value(1));
}
```

- [ ] **步骤 2：运行测试确认失败**

运行：

```powershell
mvn -f control-plane\pom.xml "-Dtest=com.agentplatform.control.agent.controller.AgentRunControllerTest" test
```

预期：`toolCalls` 仍为 0，测试失败。

- [ ] **步骤 3：实现用量统计**

在 `StoredRun.toSummary()` 中统计 `tool.completed` 事件数量，写入 `new RunUsage(modelRequests, outputEvents, toolCalls)`。

- [ ] **步骤 4：更新运行台展示**

`index.html` 的事件区域保持简单，不做复杂前端框架改造；只需要保证 SSE 事件中的工具 payload 可读。

- [ ] **步骤 5：运行控制面测试**

运行：

```powershell
mvn -f control-plane\pom.xml clean test
```

预期：控制面测试全部通过。

## 任务 4：阶段 2 端到端验收文档

**文件：**

- 创建：`docs/delivery/phase-2-showcase.md`
- 修改：`README.md`
- 修改：`task_plan.md`
- 修改：`progress.md`

- [ ] **步骤 1：创建阶段 2 展示文档**

文档必须包含：

- 阶段目标和边界。
- HTTP 工具调用演示。
- MCP tool 调用演示。
- 审计事件样例。
- 验收命令和结果。
- 已知限制。
- 阶段 3 入口条件。

- [ ] **步骤 2：更新项目入口文档**

`README.md` 增加阶段 2 成果展示链接。`task_plan.md` 将阶段 2 状态从 `planned` 更新为 `complete`，但只有在阶段 2 代码和验收完成后才能执行该步骤。

- [ ] **步骤 3：运行文档扫描**

运行：

```powershell
rg -n "TO[D]O|待[定]|占[位]|后续[补]充" docs README.md task_plan.md
```

预期：无未完成占位描述。若命中历史说明文字，必须判断是否为真实问题，不可直接忽略。

## 任务 5：最终验证

**文件：**

- 修改：`progress.md`
- 修改：`.agents/skills/agent-platform-project-governance/references/problem-log.md`

- [ ] **步骤 1：运行 Runtime 测试**

```powershell
runtime\.venv\Scripts\python -m pytest runtime\tests -q
```

预期：全部通过。

- [ ] **步骤 2：运行控制面测试**

```powershell
mvn -f control-plane\pom.xml clean test
```

预期：全部通过。

- [ ] **步骤 3：本机端到端验收**

启动 Python Runtime 和 Java 控制面后，创建包含 `echo` 和 `mcp 时间` 的 Agent Run，验收：

- 审计事件包含 `tool.requested` 和 `tool.completed`。
- SSE 中能看到工具事件。
- `usage.toolCalls` 大于 0。
- 高风险工具未开放。

- [ ] **步骤 4：归档结果**

把验证命令、结果、问题修复写入：

- `progress.md`
- `.agents/skills/agent-platform-project-governance/references/problem-log.md`
- `docs/delivery/phase-2-showcase.md`

## 开始阶段 2 前需要用户确认的信息

下面 4 项会影响阶段 2 的真实业务价值，但不影响按默认 demo 工具先做闭环：

1. **首批 HTTP 工具：** 是否使用默认 `http_echo`，还是你已有真实 HTTP API？如果有，请提供接口地址、请求方式、鉴权方式、请求/响应示例。
2. **首批 MCP server：** 是否使用默认 `mcp_local_time` 本地 demo tool，还是接入已有 MCP server？如果有，请提供启动命令、transport 类型、工具名和示例参数。
3. **工具风险策略：** 阶段 2 是否只允许 `LOW` 风险只读工具？默认答案是只允许低风险，写操作全部拒绝。
4. **依赖授权：** 是否允许新增 Python MCP SDK 或 Node MCP SDK 依赖？默认先用本地适配层完成协议抽象，等你确认后再接真实 SDK。

## 参考依据

- MCP tools 官方规范：`https://modelcontextprotocol.io/specification/2025-06-18/server/tools`
- MCP resources 官方规范：`https://modelcontextprotocol.io/specification/2025-06-18/server/resources`
- MCP prompts 官方规范：`https://modelcontextprotocol.io/specification/2025-06-18/server/prompts`
