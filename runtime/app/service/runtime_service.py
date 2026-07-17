from __future__ import annotations

from typing import Any

from fastapi import HTTPException

from app.dto.runtime import RuntimeRunRequest
from app.entity.message import Message
from app.entity.tool import ToolResult
from app.service.tool_executor import execute_tool
from app.service.tool_registry import build_tool_arguments, get_tool
from app.utils.time import utc_now


# 阶段 1 有意使用内存存储。这样可以先把运行闭环、事件顺序和短期记忆验证清楚，
# 避免在持久化工作流正式设计前引入数据库表结构和迁移成本。
RUN_EVENTS: dict[str, list[dict[str, Any]]] = {}
CONVERSATION_MESSAGES: dict[str, list[Message]] = {}
RUN_REQUESTS: dict[str, RuntimeRunRequest] = {}


def append_event(
    events: list[dict[str, Any]],
    request: RuntimeRunRequest,
    event_type: str,
    payload: dict[str, Any] | None = None,
) -> None:
    """追加一条具有稳定顺序号的运行事件。

    Java 控制面会把这些事件作为审计数据展示，因此 Runtime 即使在内存阶段也要
    保证同一个 run 内的 sequence 单调递增。
    """

    sequence = len(events) + 1
    events.append(
        {
            "eventId": f"{request.runId}_evt_{sequence:04d}",
            "traceId": request.traceId,
            "runId": request.runId,
            "type": event_type,
            "sequence": sequence,
            "timestamp": utc_now(),
            "payload": payload or {},
        }
    )


def generate_local_response(
    request: RuntimeRunRequest,
    tool_results: list[ToolResult] | None = None,
) -> str:
    """生成阶段 1 使用的确定性本地模型回复。

    这里不是测试里的 mock，而是一个明确命名的开发模型供应商。这样可以在没有
    外部模型凭据的情况下验证模型调用链路、短期记忆和审计事件。
    """

    conversation = CONVERSATION_MESSAGES[request.context.conversationId]
    user_turns = sum(1 for message in conversation if message.role == "user")
    response = (
        f"【本地开发模型】第 {user_turns} 轮收到：{request.input.text}。"
        f"Agent={request.agentSnapshot.agentId}，Trace={request.traceId}。"
    )
    if tool_results:
        tool_summaries = "；".join(
            f"{result.tool_name}={result.output}" for result in tool_results
        )
        response += f" 工具结果：{tool_summaries}。"
    return response


def select_demo_tools(request: RuntimeRunRequest) -> list[str]:
    """根据阶段 2 demo 规则选择本次运行要调用的工具。

    选择过程同时检查控制面下发的 enabledTools 白名单。这样即使用户输入中包含
    工具触发词，Runtime 也不会调用未授权工具。
    """

    enabled = set(request.agentSnapshot.enabledTools)
    text = request.input.text
    lowered_text = text.lower()
    selected: list[str] = []

    if "http_echo" in enabled and ("echo" in lowered_text or "回显" in text):
        selected.append("http_echo")
    if "mcp_local_time" in enabled and (
        ("mcp" in lowered_text and ("时间" in text or "time" in lowered_text))
        or "当前时间" in text
    ):
        selected.append("mcp_local_time")

    return selected


def execute_run(request: RuntimeRunRequest) -> None:
    """同步执行一次阶段 1 Agent Run。

    后续阶段可以把这里替换为持久化异步执行。阶段 1 保持同步，是为了让测试和
    本机演示具备确定性。
    """

    events = RUN_EVENTS.setdefault(request.runId, [])
    if events:
        # 幂等边界：同一个 runId 重复提交时，不重复产生模型调用和记忆记录。
        return

    conversation_id = request.context.conversationId
    conversation = CONVERSATION_MESSAGES.setdefault(conversation_id, [])

    append_event(events, request, "run.started", {"runtimeId": "runtime_py_local"})
    conversation.append(
        Message(
            role="user",
            content=request.input.text,
            runId=request.runId,
            timestamp=utc_now(),
        )
    )

    append_event(
        events,
        request,
        "model.requested",
        {
            "provider": request.agentSnapshot.model.provider,
            "modelName": request.agentSnapshot.model.modelName,
        },
    )
    tool_results: list[ToolResult] = []
    for tool_name in select_demo_tools(request):
        tool = get_tool(tool_name)
        if tool is None:
            append_event(
                events,
                request,
                "tool.failed",
                {
                    "toolName": tool_name,
                    "reason": "工具未注册",
                },
            )
            continue

        arguments = build_tool_arguments(tool.name, request.input.text)
        append_event(
            events,
            request,
            "tool.requested",
            {
                "toolName": tool.name,
                "sourceType": tool.source_type,
                "riskLevel": tool.risk_level,
                "input": arguments,
            },
        )
        try:
            result = execute_tool(tool, arguments)
        except ValueError as exc:
            append_event(
                events,
                request,
                "tool.failed",
                {
                    "toolName": tool.name,
                    "sourceType": tool.source_type,
                    "riskLevel": tool.risk_level,
                    "reason": str(exc),
                },
            )
            continue

        tool_results.append(result)
        append_event(
            events,
            request,
            "tool.completed",
            {
                "toolName": result.tool_name,
                "sourceType": result.source_type,
                "riskLevel": result.risk_level,
                "output": result.output,
            },
        )

    assistant_text = generate_local_response(request, tool_results)
    append_event(events, request, "run.output.delta", {"text": assistant_text})
    append_event(
        events,
        request,
        "model.completed",
        {
            "inputCharacters": len(request.input.text),
            "outputCharacters": len(assistant_text),
        },
    )

    conversation.append(
        Message(
            role="assistant",
            content=assistant_text,
            runId=request.runId,
            timestamp=utc_now(),
        )
    )
    append_event(events, request, "run.completed", {"status": "completed"})


def submit_run(request: RuntimeRunRequest) -> dict[str, str]:
    """接收并执行阶段 1 Agent Run。"""

    RUN_REQUESTS[request.runId] = request
    execute_run(request)
    return {"runId": request.runId, "status": "accepted"}


def get_run_events(run_id: str) -> dict[str, list[dict[str, Any]]]:
    """按 sequence 顺序返回指定 run 的全部 Runtime 事件。"""

    if run_id not in RUN_EVENTS:
        raise HTTPException(status_code=404, detail=f"Run not found: {run_id}")
    return {"events": RUN_EVENTS[run_id]}


def get_conversation_messages(conversation_id: str) -> dict[str, list[dict[str, Any]]]:
    """返回阶段 1 可检查的会话级短期记忆。"""

    messages = CONVERSATION_MESSAGES.get(conversation_id, [])
    return {"messages": [message.model_dump() for message in messages]}
