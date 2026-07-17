# 阶段 2 成果展示：工具系统与 MCP v1

## 阶段目标

在不破坏阶段 1 Agent Run 闭环的前提下，让 Agent 可以调用一个低风险 HTTP 工具和一个 MCP tool，并让控制面展示完整审计事件、SSE 事件和工具调用统计。

## 阶段边界

本阶段只做工具调用最小闭环，不接入真实企业系统写操作，不接入 MCP resources/prompts，不实现 RAG、长期记忆、Skill 运行时或 CLI 沙箱。

默认工具如下：

| 工具名 | 来源 | 风险 | 用途 |
|---|---|---|---|
| `http_echo` | HTTP demo | LOW | 回显输入文本并返回长度，验证 HTTP 工具链路 |
| `mcp_local_time` | MCP tool demo | LOW | 返回 `Asia/Shanghai` 当前时间，验证 MCP tool 抽象 |

## 本阶段已交付

| 交付物 | 路径 | 用途 |
|---|---|---|
| 控制面 Tool Registry | `control-plane/src/main/java/com/agentplatform/control/agent/controller/ToolRegistryController.java` | 提供 `GET /api/v1/tools` 工具列表 |
| 工具元数据 DTO | `control-plane/src/main/java/com/agentplatform/control/agent/dto/ToolDefinition.java`、`ToolRiskLevel.java` | 表达工具名称、来源、输入 schema 和风险等级 |
| 控制面工具统计 | `control-plane/src/main/java/com/agentplatform/control/agent/entity/StoredRun.java` | 从 `tool.completed` 事件派生 `usage.toolCalls` |
| Runtime 工具注册表 | `runtime/app/service/tool_registry.py` | 定义 Runtime 可执行的 demo 工具 |
| Runtime 工具执行器 | `runtime/app/service/tool_executor.py` | 只允许执行 LOW 风险工具 |
| MCP tool 本地适配层 | `runtime/app/service/mcp_tool_client.py` | 固定 MCP tool 调用边界，后续可替换为真实 MCP SDK client |
| Runtime 工具实体 | `runtime/app/entity/tool.py` | 定义工具元数据和工具结果结构 |
| 阶段 2 运行台 | `control-plane/src/main/resources/static/index.html` | 默认输入可触发 `http_echo` 和 `mcp_local_time` |
| 自动化测试 | `runtime/tests/test_runtime.py`、`control-plane/src/test/java/com/agentplatform/control/agent/controller` | 覆盖 Tool Registry、工具事件和工具统计 |

## 接口展示

工具列表接口：

```text
GET /api/v1/tools
```

返回结构包含根节点 `tools`，每个工具包含：

- `name`
- `displayName`
- `description`
- `sourceType`
- `riskLevel`
- `inputSchema`

Agent Run 审计事件新增：

```text
tool.requested
tool.completed
tool.failed
```

运行摘要新增有效工具调用统计：

```json
{
  "usage": {
    "modelRequests": 1,
    "outputEvents": 1,
    "toolCalls": 2
  }
}
```

## 端到端验收结果

本机临时端口验收：

- Python Runtime：`http://127.0.0.1:18091`
- Java 控制面：`http://127.0.0.1:18092`

工具列表验收返回：

```json
{
  "toolNames": [
    "http_echo",
    "mcp_local_time"
  ]
}
```

创建包含 `echo` 和 `mcp 时间` 的 Agent Run 后，审计事件顺序为：

```json
[
  "run.started",
  "model.requested",
  "tool.requested",
  "tool.completed",
  "tool.requested",
  "tool.completed",
  "run.output.delta",
  "model.completed",
  "run.completed"
]
```

端到端取证结果：

```json
{
  "runId": "run_6b5069546a464e6d8d431bca144b7902",
  "summaryStatus": "completed",
  "usage": {
    "modelRequests": 1,
    "outputEvents": 1,
    "toolCalls": 2
  },
  "toolCompleted": [
    "http_echo",
    "mcp_local_time"
  ],
  "sseContainsToolCompleted": true,
  "sseContentType": "text/event-stream;charset=UTF-8"
}
```

## 验收命令与结果

| 验收项 | 命令 | 结果 |
|---|---|---|
| Runtime 自动化测试 | `runtime\.venv\Scripts\python -m pytest runtime\tests -q` | pass，`4 passed` |
| 控制面自动化测试 | `mvn -f control-plane\pom.xml clean test` | pass，`Tests run: 8, Failures: 0, Errors: 0, Skipped: 0` |
| Runtime 健康检查 | `GET http://127.0.0.1:18091/health` | pass，HTTP 200 |
| 控制面运行台 | `GET http://127.0.0.1:18092/index.html` | pass，HTTP 200 |
| 端到端工具调用 | 创建包含 `echo` 和 `mcp 时间` 的 Agent Run | pass，`toolCalls=2`，SSE 包含工具事件 |

## 已知限制

| 限制 | 后续阶段 |
|---|---|
| Tool Registry 仍为内存固定 demo 配置 | 阶段 7 前逐步替换为持久化注册表和权限模型 |
| MCP tool 使用本地适配层，没有接真实 MCP server | 后续接入真实 MCP SDK 和 transport |
| 当前只允许 LOW 风险工具 | 阶段 7 引入 RBAC、审批和 Guardrails 后再开放中高风险工具 |
| 工具触发仍是确定性关键词规则 | 后续模型网关阶段可接 function calling / tool calling |
| `mcp_local_time` 只承诺 `Asia/Shanghai` demo 时区 | 接真实 MCP server 后由外部服务处理完整时区能力 |
| 运行、审计和短期记忆仍为内存存储 | 持久化工作流阶段替换 |

## 阶段 3 入口条件

阶段 3 可以在用户明确要求后开始，入口条件如下：

1. 保持阶段 1-2 的 Agent Run、SSE、审计事件和工具统计 API 不破坏。
2. RAG 文档导入必须先定义文档来源、切分策略、Embedding 供应商和权限边界。
3. 检索结果必须带来源引用，不能只返回无出处文本。
4. 知识权限默认收紧，越权知识不能进入检索上下文。

阶段 3 只做 RAG 与企业知识库，不提前实现长期记忆、Skill 运行时或 CLI。
