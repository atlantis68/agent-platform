from __future__ import annotations

from typing import Any

from fastapi import APIRouter

from app.dto.runtime import RuntimeRunRequest
from app.service import runtime_service


router = APIRouter()


@router.get("/health")
def health() -> dict[str, str]:
    """本机演示和控制面健康检查使用的 Runtime 健康接口。"""

    return {"status": "ok", "runtimeId": "runtime_py_local"}


@router.post("/internal/v1/runs")
def submit_run(request: RuntimeRunRequest) -> dict[str, str]:
    """接收控制面提交的阶段 1 Agent Run。"""

    return runtime_service.submit_run(request)


@router.get("/internal/v1/runs/{run_id}/events")
def get_run_events(run_id: str) -> dict[str, list[dict[str, Any]]]:
    """返回指定运行的全部 Runtime 事件。"""

    return runtime_service.get_run_events(run_id)


@router.get("/internal/v1/conversations/{conversation_id}/messages")
def get_conversation_messages(conversation_id: str) -> dict[str, list[dict[str, Any]]]:
    """暴露短期记忆，供阶段 1 验收和本机调试使用。"""

    return runtime_service.get_conversation_messages(conversation_id)
