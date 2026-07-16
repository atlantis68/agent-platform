# Findings

## 2026-07-15 调研依据

- OpenAI Agents SDK 官方文档将 Agent Runtime 的关键能力归纳为 turns、tool execution、guardrails、handoffs、sessions、tracing 等能力，这支持本平台把执行面拆成模型调用、工具调用、会话记忆、保护策略与追踪审计。
  来源：https://openai.github.io/openai-agents-python/

- OpenAI Agents SDK 的 Tracing 文档说明一次 Agent 运行应记录 LLM generations、tool calls、handoffs、guardrails 与自定义事件。本平台第 0 阶段将 trace/audit 作为协议基线，而不是后补能力。
  来源：https://openai.github.io/openai-agents-python/tracing/

- LangGraph 官方文档将持久化拆为 checkpointers 与 stores：checkpointers 面向线程级短期状态，stores 面向跨线程长期记忆。本平台按短期记忆与长期记忆分别规划阶段 1 和阶段 4。
  来源：https://docs.langchain.com/oss/python/langgraph/persistence

- LangGraph 官方概览强调 durable execution、streaming、human-in-the-loop、persistence。本平台将持久化工作流、流式事件、人工介入放入平台必备能力。
  来源：https://docs.langchain.com/oss/python/langgraph/overview

- MCP 官方规范定义 MCP 用于连接外部数据源和工具，并通过 tools、resources、prompts、authorization 等能力暴露上下文与操作。本平台阶段 2 先接入 MCP tools，后续扩展 resources/prompts/authorization。
  来源：https://modelcontextprotocol.io/specification/2025-11-25

- MCP Tools 规范说明工具包含名称、描述和输入 schema。本平台 Tool Registry 必须保存工具元数据、参数 schema、权限等级与审计策略。
  来源：https://modelcontextprotocol.io/specification/2025-11-25/server/tools

## 2026-07-16 阶段 2 规划补充依据

- MCP 官方 Tools 规范说明客户端通过 `tools/list` 发现工具，通过 `tools/call` 调用工具，工具元数据包含 `inputSchema`。阶段 2 因此优先实现 Tool Registry、工具输入 schema 和工具调用审计。
  来源：https://modelcontextprotocol.io/specification/2025-06-18/server/tools

- MCP 官方 Resources 与 Prompts 规范分别定义 `resources/list`、`resources/read`、`prompts/list`、`prompts/get` 等能力。阶段 2 明确不接入 resources/prompts，避免把工具阶段扩大成 RAG、提示词市场或长期上下文阶段。
  来源：https://modelcontextprotocol.io/specification/2025-06-18/server/resources
  来源：https://modelcontextprotocol.io/specification/2025-06-18/server/prompts
