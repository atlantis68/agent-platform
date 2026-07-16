# 企业内部通用 Agent 平台

本项目采用混合架构建设企业内部通用 Agent 平台：Java/Spring Boot 负责控制面，Python/FastAPI 负责 Agent Runtime，前端负责配置、运行、审计与展示。

## 当前阶段

- 阶段 0：架构与协议基线，已交付文档基线。
- 阶段 1：最小 Agent Runtime 闭环，已完成本机交付、修复收口和用户验收。
- 下一阶段：阶段 2，工具系统与 MCP v1；已完成实施计划，尚未开始编码，需用户明确要求后再执行。

## 阶段 2 启动建议

阶段 2 的目标不是一次性接入所有真实工具，而是先打通 Agent 调用工具的最小闭环。推荐默认使用 `http_echo` 和 `mcp_local_time` 两个低风险 demo 工具完成协议、审计和展示链路，等闭环稳定后再替换为真实 HTTP API 或真实 MCP server。

阶段 2 建议按以下顺序推进：

1. 先实现控制面 Tool Registry，固定工具元数据、输入 schema、来源类型和风险等级。
2. 再实现 Runtime 工具注册与执行，确保每次工具调用都产生 `tool.requested`、`tool.completed` 或 `tool.failed` 事件。
3. 然后补齐控制面运行摘要、审计查询和 SSE 展示，保证工具事件可追踪。
4. 最后做端到端验收和阶段 2 成果展示文档。

如果没有真实工具配置，可以直接按默认 demo 工具启动阶段 2。如果需要接入真实业务工具，请先提供首批 HTTP 工具、首批 MCP server、工具风险策略和是否允许新增 SDK 依赖。

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
