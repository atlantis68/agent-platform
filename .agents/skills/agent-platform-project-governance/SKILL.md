---
name: agent-platform-project-governance
description: Use when Codex works on the enterprise Agent platform under D:\Workspace\agent-platform, especially planning or implementing phases, changing architecture or protocol docs, writing code that needs readable comments, handling local-only development without git commits, or recording problems, fixes, verifications, and prevention notes for this project.
---

# Agent 平台项目治理

## 概述

本技能保存 `D:\Workspace\agent-platform` 的项目级约束和问题沉淀方法。任何继续该项目的 Agent 都必须先遵守本技能，再进行阶段规划、编码、验证或文档更新。

## 不可违反的项目约束

| 约束 | 要求 |
|---|---|
| 本机开发 | 当前项目只在本机 `D:\Workspace\agent-platform` 开发和验证 |
| Git 写操作授权 | 默认不执行 `git init`、`git add`、`git commit`、`git push`、`git merge`、`git tag` 等写操作；只有用户明确授权时才可执行 |
| Git 只读例外 | 未获授权时仅允许 `git status`、`git diff`、`git log` 等只读检查；2026-07-16 用户已授权本次初始化仓库、提交并推送到 GitHub |
| 注释完整 | 开发阶段注释要便于人工阅读和交接，所有代码备注统一使用中文 |
| 问题沉淀 | 遇到问题必须更新 `references/problem-log.md` |
| 阶段交付 | 每阶段必须有成果展示、交付物和验收标准 |
| 交互归档 | 后续开发阶段交互必须详细说明为什么这样做、做了什么和验证结果 |
| 人工介入 | 需要人工确认时必须暂停相关主链路，确认清楚后再继续 |

## 每次开始工作

1. 读取项目根目录 `AGENTS.md`。
2. 读取 `task_plan.md`、`progress.md`、`findings.md`，确认当前阶段和既有决策。
3. 若任务涉及已知问题、错误复现、环境异常或规则变更，读取 `references/problem-log.md`。
4. 不要主动建议或执行 Git 写操作；用户明确授权前，交付以本机文件和验证结果为准。
5. 对用户同步进展时，说明当前为什么这样做、已经做了什么、验证结果是什么、归档在哪里。

## 交互归档标准

后续开发阶段的关键回复和阶段总结必须包含：

- 为什么：说明当前方案、顺序或工具选择的原因。
- 做了什么：列出关键文件、关键命令、关键配置或关键验证。
- 结果如何：说明验证输出、通过/失败状态和仍存在的限制。
- 归档位置：指出对应的 `progress.md`、阶段展示文档或 `references/problem-log.md`。
- 下一步：说明下一步准备推进的内容。

不要只回复“已完成”“已修复”“已更新”。这些结论必须有事实、文件和验证支撑。

## 人工介入标准

遇到以下情况，暂停相关主链路并等待用户确认：

- 需求、范围、接口或技术路线存在会影响实现的歧义。
- 需要执行 Git 写操作、删除/覆盖文件、数据库变更、外部系统写入、危险 CLI 命令。
- 缺少必要凭据、模型配置、数据库连接、第三方服务授权或运行环境。
- 构建、测试、启动连续失败，继续尝试可能扩大影响或偏离计划。
- 项目规则之间冲突，或用户新要求与既有规则不一致。

反馈格式必须包含：阻塞点、影响范围、已确认事实、需要用户确认的问题、确认前不会继续执行的动作。

## 注释标准

所有代码备注必须使用中文，包括 JavaDoc、Python docstring、行注释、块注释和测试辅助说明。Agent、Runtime、DTO、HTTP、SSE、MCP 等业界通用英文技术名词可以保留，但解释性句子必须使用中文。

写代码时优先注释这些内容：

- 公共 API、DTO、事件、协议字段的业务含义和兼容性要求。
- Agent Run、模型调用、工具调用、RAG、记忆、Skill、CLI 的核心流程。
- 权限、审批、Guardrail、沙箱、脱敏、重试、幂等、回滚等风险边界。
- 非显而易见的设计取舍，例如为什么先用 HTTP/SSE 而不是 MQ/gRPC。
- 复杂测试的意图和验收点。

不要写空洞注释，例如“设置变量”“调用方法”。注释必须解释原因、边界或人工维护时容易误解的点。

## 问题沉淀流程

遇到问题后，按以下顺序处理：

1. 先同步硬阻塞：如果主链路需要人工介入，立即告诉用户阻塞点、影响范围和所需动作。
2. 修复前记录事实：错误信息、触发命令、相关文件、当前阶段。
3. 修复后验证：运行能证明问题已解决的检查、测试或扫描。
4. 更新 `references/problem-log.md`，不要只写进对话。
5. 若问题影响阶段计划，更新项目 `task_plan.md` 或 `progress.md`。

使用这个模板追加问题：

```markdown
## YYYY-MM-DD HH:mm - 简短标题

- 阶段：
- 现象：
- 影响：
- 根因：
- 修复：
- 验证：
- 预防：
- 关联文件：
```

## 阶段交付检查

每个阶段结束前确认：

- 有成果展示文档或可运行演示。
- 有交付物清单。
- 有验收标准和验证结果。
- 有已知限制。
- 有下一阶段入口条件。
- 新问题已进入 `references/problem-log.md`。

## 常见错误

| 错误 | 正确做法 |
|---|---|
| 看到项目没 Git 就建议初始化并提交 | 当前规则是 Git 写操作必须先获得用户明确授权 |
| 只在最终回答里说明问题 | 同步到 `problem-log.md` |
| 代码注释过少 | 为接口、流程、风险边界和设计原因补充注释 |
| 代码备注使用英文叙述 | 改为中文表达，只保留必要的英文技术名词 |
| 阶段结束没有展示材料 | 补齐 `docs/delivery/phase-N-showcase.md` 或等价展示 |
| 只记录修复，不记录验证 | 每条问题记录必须包含验证方式 |
| 只说“已完成”不解释原因和动作 | 说明为什么、做了什么、验证了什么、归档在哪里 |
| 需要人工介入时继续推进 | 暂停相关主链路，等用户确认后再继续 |

## 参考资料

- 详细问题记录：`references/problem-log.md`
