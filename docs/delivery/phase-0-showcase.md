# 阶段 0 成果展示：架构与协议基线

## 阶段目标

完成企业内部通用 Agent 平台的技术基线，让后续阶段可以并行拆分控制面、Runtime、前端、工具、知识库和治理能力。

## 本阶段已交付

| 交付物 | 路径 | 用途 |
|---|---|---|
| 项目说明 | `README.md` | 说明平台目标、当前阶段和关键文档入口 |
| 分阶段路线 | `docs/roadmap/phased-delivery-plan.md` | 锁定 0-8 阶段目标、展示、交付物和验收标准 |
| 架构总览 | `docs/architecture/00-overview.md` | 描述混合架构、子系统职责、运行主链路 |
| Runtime 协议 | `docs/architecture/01-runtime-contracts.md` | 定义控制面与 Runtime 的 API、事件、工具调用协议 |
| 实施计划 | `docs/superpowers/plans/2026-07-15-agent-platform-phase0.md` | 记录阶段 0 如何实施和验收 |
| 持久化计划 | `task_plan.md`、`findings.md`、`progress.md` | 保存长期项目上下文、调研依据和进度 |

## 展示方式

### 1. 架构图展示

打开 `docs/architecture/00-overview.md`，查看总体架构图和一次运行主链路。重点展示：

- Java 控制面和 Python Runtime 的边界。
- 模型网关、工具网关、RAG、记忆、Skill、CLI 的位置。
- 所有关键动作都进入 Trace/Audit。
F
### 2. 协议样例展示

打开 `docs/architecture/01-runtime-contracts.md`，展示：

- `POST /api/v1/agent-runs` 如何创建运行。
- `GET /api/v1/agent-runs/{runId}/events` 如何返回 SSE 事件。
- `POST /internal/v1/tool-invocations` 如何请求工具调用。
- `approval_required` 如何为阶段 7 的人工介入预留协议。

### 3. 阶段路线展示

打开 `docs/roadmap/phased-delivery-plan.md`，逐阶段确认：

- 每阶段的目标。
- 每阶段的成果展示方式。
- 每阶段的交付物。
- 每阶段的验收标准。

## 验收标准

| 标准 | 状态 |
|---|---|
| 能说明为什么采用 Java 控制面 + Python Runtime | pass |
| 能说明控制面与 Runtime 的职责边界 | pass |
| 能说明一次 Agent Run 的主链路 | pass |
| 能说明阶段 1 应该实现哪些接口 | pass |
| 能说明每个阶段的展示和交付物 | pass |
| 协议为短期记忆、RAG、长期记忆、MCP、Skill、CLI 预留事件类型 | pass |

## 已知限制

| 限制 | 处理阶段 |
|---|---|
| 当前阶段只交付架构与协议，不交付可运行代码 | 阶段 1 |
| Tool Registry 数据模型尚未细化 | 阶段 2 |
| RAG 文档处理和权限过滤尚未设计到表结构 | 阶段 3 |
| 长期记忆写入策略尚未细化到评分模型 | 阶段 4 |
| CLI 沙箱尚未选择具体隔离技术 | 阶段 6 |

## 下一阶段入口条件

阶段 1 可以开始，入口条件如下：

1. 使用本阶段的 Runtime 协议作为接口基线。
2. 先实现单 Agent、单模型、单用户路径。
3. 先使用内存或简单数据库保存短期运行状态，保留后续替换为持久化工作流的接口。
4. 暂不开放工具、RAG、长期记忆、Skill、CLI，只保留事件类型兼容。

