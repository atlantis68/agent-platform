from __future__ import annotations

from typing import Literal

from pydantic import BaseModel


class Message(BaseModel):
    """会话级短期记忆中的一条消息。"""

    role: Literal["user", "assistant"]
    content: str
    runId: str
    timestamp: str
