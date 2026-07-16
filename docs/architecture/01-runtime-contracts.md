# Runtime 协议基线

## 设计目标

本协议用于锁定 Java 控制面与 Python Runtime 的边界。阶段 1 起所有实现必须遵守这些基础字段和事件类型，后续阶段只能兼容扩展，不能破坏既有字段语义。

## 标识字段

| 字段 | 类型 | 说明 |
|---|---|---|
| `traceId` | string | 一次用户请求的全链路追踪 ID |
| `runId` | string | 一次 Agent 运行 ID |
| `tenantId` | string | 租户 ID |
| `userId` | string | 发起用户 ID |
| `agentId` | string | Agent ID |
| `agentVersion` | string | Agent 配置版本 |
| `runtimeId` | string | Runtime 实例 ID |
| `idempotencyKey` | string | 幂等键，用于防止重复创建运行 |

## 控制面 API

### 创建运行

`POST /api/v1/agent-runs`

```json
{
  "agentId": "agent_general_001",
  "input": {
    "type": "text",
    "text": "帮我总结这份制度文档"
  },
  "context": {
    "conversationId": "conv_001",
    "source": "web"
  },
  "idempotencyKey": "20260715-user-001-run-001"
}
```

响应：

```json
{
  "traceId": "tr_20260715_000001",
  "runId": "run_20260715_000001",
  "status": "queued",
  "eventStreamUrl": "/api/v1/agent-runs/run_20260715_000001/events"
}
```

### 查询运行

`GET /api/v1/agent-runs/{runId}`

响应：

```json
{
  "traceId": "tr_20260715_000001",
  "runId": "run_20260715_000001",
  "status": "completed",
  "agentId": "agent_general_001",
  "startedAt": "2026-07-15T09:00:00Z",
  "completedAt": "2026-07-15T09:00:08Z",
  "usage": {
    "modelRequests": 2,
    "inputTokens": 1200,
    "outputTokens": 380,
    "toolCalls": 1
  }
}
```

### 流式事件

`GET /api/v1/agent-runs/{runId}/events`

协议：Server-Sent Events。事件体使用 `RunEvent`。

### 取消运行

`POST /api/v1/agent-runs/{runId}/cancel`

响应：

```json
{
  "runId": "run_20260715_000001",
  "status": "cancelling"
}
```

## Runtime 内部 API

### 提交执行

`POST /internal/v1/runs`

```json
{
  "traceId": "tr_20260715_000001",
  "runId": "run_20260715_000001",
  "tenantId": "tenant_default",
  "userId": "user_001",
  "agentSnapshot": {
    "agentId": "agent_general_001",
    "agentVersion": "1.0.0",
    "name": "企业通用助手",
    "systemPrompt": "你是企业内部通用助手。",
    "model": {
      "provider": "openai-compatible",
      "modelName": "gpt-5",
      "temperature": 0.2
    },
    "enabledTools": [],
    "enabledSkills": []
  },
  "input": {
    "type": "text",
    "text": "帮我总结这份制度文档"
  }
}
```

响应：

```json
{
  "runId": "run_20260715_000001",
  "status": "accepted"
}
```

### 恢复执行

`POST /internal/v1/runs/{runId}/resume`

用于人工审批、长任务恢复和中断恢复。

```json
{
  "traceId": "tr_20260715_000001",
  "resumeToken": "resume_001",
  "decision": {
    "type": "approved",
    "approvedBy": "user_approver_001"
  }
}
```

## RunEvent

所有事件使用统一结构：

```json
{
  "eventId": "evt_000001",
  "traceId": "tr_20260715_000001",
  "runId": "run_20260715_000001",
  "type": "run.started",
  "sequence": 1,
  "timestamp": "2026-07-15T09:00:00Z",
  "payload": {}
}
```

## 事件类型

| 类型 | 阶段 | 说明 |
|---|---|---|
| `run.created` | 1 | 控制面创建运行 |
| `run.started` | 1 | Runtime 开始执行 |
| `run.output.delta` | 1 | 流式文本增量 |
| `run.completed` | 1 | 运行成功完成 |
| `run.failed` | 1 | 运行失败 |
| `run.cancelled` | 1 | 运行取消 |
| `model.requested` | 1 | 发起模型调用 |
| `model.completed` | 1 | 模型调用完成 |
| `tool.requested` | 2 | Runtime 请求工具调用 |
| `tool.approval_required` | 2 | 工具调用需要审批 |
| `tool.completed` | 2 | 工具调用完成 |
| `tool.failed` | 2 | 工具调用失败 |
| `rag.retrieved` | 3 | RAG 完成检索 |
| `memory.read` | 4 | 读取长期记忆 |
| `memory.write_proposed` | 4 | Runtime 提议写入长期记忆 |
| `memory.written` | 4 | 长期记忆写入完成 |
| `skill.selected` | 5 | 选择 Skill |
| `cli.command_requested` | 6 | 请求执行 CLI 命令 |
| `cli.command_completed` | 6 | CLI 命令完成 |
| `guardrail.blocked` | 7 | Guardrail 阻断 |
| `handoff.requested` | 7 | 请求转交给其他 Agent |

## 工具调用请求

`POST /internal/v1/tool-invocations`

```json
{
  "traceId": "tr_20260715_000001",
  "runId": "run_20260715_000001",
  "toolName": "policy.search",
  "riskLevel": "read",
  "arguments": {
    "keyword": "差旅报销"
  },
  "requestedBy": {
    "agentId": "agent_general_001",
    "runtimeId": "runtime_py_001"
  }
}
```

响应：

```json
{
  "invocationId": "tool_call_001",
  "status": "completed",
  "result": {
    "items": []
  }
}
```

需要审批时：

```json
{
  "invocationId": "tool_call_002",
  "status": "approval_required",
  "resumeToken": "resume_001",
  "approvalReason": "工具风险等级为 write，需要人工确认"
}
```

## 风险等级

| 等级 | 说明 | 默认策略 |
|---|---|---|
| `read` | 只读查询 | 允许，需审计 |
| `write` | 修改业务数据 | 需要审批 |
| `dangerous` | 删除、付款、外发、执行命令、批量变更 | 默认拒绝，按场景单独开通 |

## 兼容规则

1. 新增事件类型必须保留统一 `RunEvent` 结构。
2. 新增 payload 字段必须向后兼容。
3. `traceId`、`runId`、`sequence`、`type` 不得缺失。
4. Runtime 不得绕过控制面直接执行企业写操作工具。
5. Runtime 写入长期记忆前必须先产生 `memory.write_proposed`。

