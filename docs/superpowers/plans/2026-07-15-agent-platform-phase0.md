# Agent 平台阶段 0 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 交付企业内部通用 Agent 平台的架构与协议基线，为阶段 1 的最小 Runtime 闭环提供清晰边界。

**架构：** 采用 Java/Spring Boot 控制面 + Python/FastAPI Runtime 的混合架构。控制面负责企业治理和任务管理，Runtime 负责 Agent 执行、模型调用和能力编排。

**技术栈：** Java/Spring Boot、Python/FastAPI、Vue 3、PostgreSQL/MySQL、Redis、HTTP、SSE、MCP。

---

## 文件结构

| 文件 | 职责 |
|---|---|
| `README.md` | 项目入口说明 |
| `task_plan.md` | 长期项目阶段计划 |
| `findings.md` | 调研依据 |
| `progress.md` | 执行进度 |
| `docs/roadmap/phased-delivery-plan.md` | 0-8 阶段路线和验收 |
| `docs/architecture/00-overview.md` | 架构总览和主链路 |
| `docs/architecture/01-runtime-contracts.md` | 控制面与 Runtime 协议 |
| `docs/delivery/phase-0-showcase.md` | 阶段 0 成果展示和验收 |

### 任务 1：创建项目入口与持久化计划

**文件：**
- 创建：`README.md`
- 创建：`task_plan.md`
- 创建：`findings.md`
- 创建：`progress.md`

- [x] **步骤 1：创建项目目录**

运行：

```powershell
New-Item -ItemType Directory -Force -Path D:\Workspace\agent-platform
```

预期：目录存在。

- [x] **步骤 2：创建项目入口说明**

写入 `README.md`，包含平台定位、当前阶段、核心能力目标和关键文档入口。

- [x] **步骤 3：创建长期计划文件**

写入 `task_plan.md`，包含阶段 0-8 的状态、交付物和关键技术决策。

- [x] **步骤 4：记录调研依据**

写入 `findings.md`，记录 OpenAI Agents SDK、LangGraph、MCP 官方文档对平台能力拆分的依据。

- [x] **步骤 5：记录执行进度**

写入 `progress.md`，记录阶段 0 已创建的文档。

### 任务 2：固化分阶段路线

**文件：**
- 创建：`docs/roadmap/phased-delivery-plan.md`

- [x] **步骤 1：定义 0-8 阶段**

每个阶段写明名称、目标、成果展示、交付物和验收标准。

- [x] **步骤 2：定义推进规则**

每阶段必须有可展示成果、交付物、验收标准、风险限制和下一阶段入口条件。

- [x] **步骤 3：定义里程碑**

将阶段聚合为 M0-M4，便于后续项目管理和汇报。

### 任务 3：交付架构总览

**文件：**
- 创建：`docs/architecture/00-overview.md`

- [x] **步骤 1：定义平台架构定位**

明确 Java 控制面、Python Runtime、前端和关键基础设施职责。

- [x] **步骤 2：绘制总体架构图**

使用 Mermaid 展示前端、控制面、Runtime、模型网关、工具网关、RAG、记忆、Skill、CLI 的关系。

- [x] **步骤 3：定义职责边界**

用表格说明控制面保存策略和治理，Runtime 执行任务和编排能力。

- [x] **步骤 4：绘制运行主链路**

使用 Mermaid sequenceDiagram 展示从创建 Agent Run 到完成审计的链路。

### 任务 4：交付 Runtime 协议

**文件：**
- 创建：`docs/architecture/01-runtime-contracts.md`

- [x] **步骤 1：定义统一标识字段**

包含 `traceId`、`runId`、`tenantId`、`userId`、`agentId`、`agentVersion`、`runtimeId`、`idempotencyKey`。

- [x] **步骤 2：定义控制面 API**

包含创建运行、查询运行、流式事件和取消运行。

- [x] **步骤 3：定义 Runtime 内部 API**

包含提交执行和恢复执行。

- [x] **步骤 4：定义 RunEvent**

统一事件结构，包含 `eventId`、`traceId`、`runId`、`type`、`sequence`、`timestamp`、`payload`。

- [x] **步骤 5：定义阶段化事件类型**

事件类型覆盖阶段 1-7 的模型、工具、RAG、记忆、Skill、CLI、Guardrail、Handoff。

- [x] **步骤 6：定义工具调用协议**

包含工具请求、完成响应和审批响应。

### 任务 5：交付阶段 0 展示稿

**文件：**
- 创建：`docs/delivery/phase-0-showcase.md`

- [x] **步骤 1：列出本阶段交付物**

用表格说明每个交付物路径和用途。

- [x] **步骤 2：定义展示方式**

按架构图、协议样例、阶段路线三个角度展示。

- [x] **步骤 3：定义验收标准**

将阶段 0 验收项标记为 pass。

- [x] **步骤 4：列出已知限制和下一阶段入口条件**

明确阶段 1 从最小 Agent Runtime 开始，不提前开放工具、RAG、长期记忆、Skill、CLI。

## 自检结果

| 检查项 | 结果 |
|---|---|
| 规格覆盖度 | pass，覆盖阶段路线、架构、协议、展示和验收 |
| 占位符扫描 | pass，没有保留待填充章节 |
| 类型一致性 | pass，核心字段在各文档中保持一致 |
| 下一阶段可执行性 | pass，阶段 1 可基于 `POST /api/v1/agent-runs`、SSE 事件和 Runtime 内部 API 启动 |

