# Agent 平台分阶段落地计划

## Goal

从零建设一个企业内部通用 Agent 平台，采用 Java 控制面 + Python Runtime 的混合架构，并按阶段交付短期记忆、长期记忆、RAG、MCP、Skill、CLI、治理、观测与评测能力。

## Active Phase

阶段 2：工具系统与 MCP v1（complete，本机自动化与端到端验收通过）。阶段 3：RAG 与企业知识库（planned，未开始编码）。

## Next Step

下一步建议进入阶段 3：RAG 与企业知识库。当前只完成阶段 3 入口条件说明，不自动启动编码；只有在用户明确要求“开始阶段 3”后，才进入阶段 3 实施。

阶段 3 建议先做最小 RAG 闭环：

1. 先定义文档导入协议、文档元数据和权限边界。
2. 再实现本地 demo 文档切分、Embedding 抽象和检索接口。
3. 控制面展示回答来源和引用片段，确保 RAG 可审计。
4. 阶段结束时创建 `docs/delivery/phase-3-showcase.md`，并运行 Runtime、控制面和端到端验收。

阶段 3 启动前建议确认的信息：

| 信息 | 是否必需 | 默认值 | 影响 |
|---|---|---|---|
| 首批文档来源 | 否 | 本地 demo Markdown 文档 | 影响 RAG 回答的业务真实性 |
| Embedding 实现 | 否 | 本地 deterministic demo 向量或轻量本地适配 | 影响是否需要外部模型凭据 |
| 知识权限策略 | 否 | 默认只允许同租户知识 | 影响越权检索防护 |
| 向量库选择 | 否 | 先用内存或本地轻量实现 | 影响是否引入 PostgreSQL/pgvector |

## Phases

| 阶段 | 名称 | 状态 | 阶段成果 |
|---|---|---|---|
| 0 | 架构与协议基线 | complete | 架构总览、分阶段路线、Runtime 协议、阶段展示稿 |
| 1 | 最小 Agent Runtime | complete | 可创建 Agent、发起运行、流式返回、短期记忆 v1、基础审计 |
| 2 | 工具系统与 MCP v1 | complete | Tool Registry、HTTP 工具、MCP demo tool、工具审计、风险分级、SSE 展示和 `usage.toolCalls` |
| 3 | RAG 与企业知识库 | planned | 文档导入、切分、向量检索、混合检索、引用溯源、知识权限 |
| 4 | 长期记忆 | pending | 记忆类型、写入策略、检索策略、冲突处理、用户可管理 |
| 5 | Skill 能力体系 | pending | Skill Manifest、版本、依赖工具、内部市场、Skill 评测 |
| 6 | CLI 与沙箱执行 | pending | 隔离工作区、命令策略、资源限制、审批、输出脱敏 |
| 7 | 企业治理与可靠性 | pending | RBAC、Agent 身份、HITL、Guardrails、持久化工作流、多 Agent |
| 8 | 观测、评测与运营 | pending | Trace、回放、成本、质量评测、回归测试、灰度发布、运营看板 |

## Delivery Rule

每个阶段必须交付以下内容：

1. 可运行或可审查的成果展示。
2. 明确的交付物清单。
3. 验收标准。
4. 风险与限制说明。
5. 下一阶段入口条件。

## Decisions

| 决策 | 结果 |
|---|---|
| 平台架构 | 混合架构 |
| 控制面 | Java / Spring Boot |
| Runtime | Python / FastAPI |
| 前端 | Vue 3，优先贴合现有企业前端栈 |
| 初始通信 | HTTP + SSE |
| 后续通信扩展 | MQ/gRPC 在长任务和高并发阶段引入 |
| 初始数据库 | PostgreSQL 优先，便于后续引入 pgvector；企业已有 MySQL 时可替换 |
| 初始缓存 | Redis |
| 安全默认值 | 默认拒绝高风险工具与 CLI，按权限和审批逐步开放 |
| Git 策略 | Git 写操作默认需要用户明确授权；2026-07-16 用户已授权本次初始化仓库、提交并推送到 GitHub |
| 注释策略 | 开发阶段代码注释尽量完整，且所有代码备注统一使用中文；重点解释接口、流程、风险边界和设计原因 |
| 经验沉淀 | 问题、根因、修复、验证和预防措施必须沉淀到项目内 `.agents/skills/agent-platform-project-governance` Skill |
| 交互归档 | 后续开发阶段交互必须详细说明为什么这样做、做了什么、验证了什么和归档位置 |
| 人工介入 | 遇到必须人工确认的阻塞或高风险动作时，暂停主链路并等待确认 |

## Errors Encountered

| 时间 | 问题 | 处理 |
|---|---|---|
| 2026-07-15 | 无现成 agent-platform 项目目录 | 新建独立项目目录 `D:\Workspace\agent-platform` |
| 2026-07-15 | 项目目录尚未初始化 Git | 根据用户最新约束，后续不执行 Git 写操作，除非用户重新授权 |
| 2026-07-15 | 用户明确要求暂时不用 Git 提交 | 更新项目规则，后续不再建议初始化或提交 Git，除非用户重新授权 |
| 2026-07-15 | 用户要求后续交互详细说明原因和动作，并在人工介入时暂停 | 更新项目规则、Skill 和进度记录 |
| 2026-07-15 | PowerShell 不支持 Bash here-doc 语法 | 改用 `python -c` 执行 Python 包检查，并记录到项目 Skill |
| 2026-07-15 | 系统 Python 缺少 FastAPI，Runtime 测试无法收集 | 创建 `runtime\.venv`，安装并收窄 Runtime 依赖范围 |
| 2026-07-15 | Java 控制面到 Uvicorn Runtime 端到端调用返回 422/500 | 固定 Runtime HTTP 客户端为 HTTP/1.1，并显式发送 JSON |
| 2026-07-15 | 增加测试构造器后 Spring 无法选择 `HttpRuntimeClient` 生产构造器 | 给生产构造器添加 `@Autowired`，新增真实上下文启动烟测 |
| 2026-07-16 | 用户明确要求所有代码备注统一使用中文 | 更新 `AGENTS.md`、项目专属 Skill 和阶段计划；将现有 Java/Python 英文备注改为中文 |
| 2026-07-16 | 浏览器直接打开 SSE `/events` 地址时中文显示乱码 | 在控制面 SSE 写出边界强制 `text/event-stream;charset=UTF-8` 和 JSON UTF-8 data 输出，并新增回归测试 |
