from __future__ import annotations

from typing import Any

from fastapi import HTTPException

from app.dto.runtime import RuntimeRunRequest
from app.entity.message import Message
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


def generate_local_response(request: RuntimeRunRequest) -> str:
    """生成阶段 1 使用的确定性本地模型回复。

    这里不是测试里的 mock，而是一个明确命名的开发模型供应商。这样可以在没有
    外部模型凭据的情况下验证模型调用链路、短期记忆和审计事件。
    """

    conversation = CONVERSATION_MESSAGES[request.context.conversationId]
    user_turns = sum(1 for message in conversation if message.role == "user")
    return (
        f"【本地开发模型】第 {user_turns} 轮收到：{request.input.text}。"
        f"Agent={request.agentSnapshot.agentId}，Trace={request.traceId}。"
    )


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
    assistant_text = generate_local_response(request)
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
