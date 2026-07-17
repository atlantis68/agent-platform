# 企业内部通用 Agent 平台

本项目采用混合架构建设企业内部通用 Agent 平台：Java/Spring Boot 负责控制面，Python/FastAPI 负责 Agent Runtime，前端负责配置、运行、审计与展示。

## 当前阶段

- 阶段 0：架构与协议基线，已交付文档基线。
- 阶段 1：最小 Agent Runtime 闭环，已完成本机交付、修复收口和用户验收。
- 阶段 2：工具系统与 MCP v1，已完成本机交付，支持 Tool Registry、低风险 HTTP demo 工具、MCP demo tool、工具审计和 `usage.toolCalls`。
- 下一阶段：阶段 3，RAG 与企业知识库；需用户明确要求后再执行。

## 阶段 2 已交付能力

阶段 2 的目标不是一次性接入所有真实工具，而是先打通 Agent 调用工具的最小闭环。本阶段已经使用 `http_echo` 和 `mcp_local_time` 两个低风险 demo 工具完成协议、审计和展示链路。

阶段 2 已按以下顺序落地：

1. 控制面 Tool Registry：固定工具元数据、输入 schema、来源类型和风险等级。
2. Runtime 工具注册与执行：每次工具调用产生 `tool.requested`、`tool.completed` 或 `tool.failed` 事件。
3. 控制面运行摘要与展示：审计查询、SSE 和 `usage.toolCalls` 均可看到工具调用。
4. 阶段 2 成果展示：记录自动化验证和本机端到端验收结果。

真实 HTTP API 和真实 MCP server 尚未接入。后续接入前，需要先提供接口配置、鉴权方式、风险等级和是否允许新增 SDK 依赖。

## 项目执行约束

- 当前仅在本机开发和验证，暂时不执行 Git 提交、推送、合并或发布。
- Git 写操作默认需要用户明确授权；2026-07-16 用户已授权本次提交并推送到 [atlantis68/agent-platform](https://github.com/atlantis68/agent-platform.git)。
- 代码注释需要尽量完整，重点解释公共接口、核心流程、风险边界和非显而易见的设计决策。
- 过程中遇到的问题和修复方式必须沉淀到项目专属 Skill：`agent-platform-project-governance`。
- 详细规则见 [AGENTS.md](AGENTS.md)。

## 核心能力目标

- 短期记忆：会话级上下文、运行状态、上下文裁剪。
- 长期记忆：跨会话偏好、事实、历史结论、经验沉淀。
- RAG：企业知识库检索、引用溯源、权限过滤。
- MCP：标准化接入外部工具、数据源与工作流。
- Skill：可复用能力包、版本化、评测与发布。
- CLI：隔离工作区内的命令行自动化能力。
- 企业治理：权限、审批、审计、Guardrails、观测、评测、限流与成本。

## 关键文档

- [分阶段交付路线](docs/roadmap/phased-delivery-plan.md)
- [架构总览](docs/architecture/00-overview.md)
- [Runtime 协议基线](docs/architecture/01-runtime-contracts.md)
- [阶段 0 成果展示](docs/delivery/phase-0-showcase.md)
- [阶段 1 成果展示](docs/delivery/phase-1-showcase.md)
- [阶段 2 成果展示](docs/delivery/phase-2-showcase.md)
- [阶段 0 实施计划](docs/superpowers/plans/2026-07-15-agent-platform-phase0.md)
- [阶段 1 实施计划](docs/superpowers/plans/2026-07-15-agent-platform-phase1.md)
- [阶段 2 实施计划](docs/superpowers/plans/2026-07-16-agent-platform-phase2-tool-mcp.md)

## Windows 本机启停脚本

Python Runtime：

```powershell
.\scripts\windows\start-python-runtime.ps1
.\scripts\windows\stop-python-runtime.ps1
```

Java 控制面：

```powershell
.\scripts\windows\start-java-control-plane.ps1
.\scripts\windows\stop-java-control-plane.ps1
```

脚本默认使用 `127.0.0.1:8001` 作为 Python Runtime 地址，`127.0.0.1:8080` 作为 Java 控制面地址；运行态 PID 和日志写入 `.run/`。
