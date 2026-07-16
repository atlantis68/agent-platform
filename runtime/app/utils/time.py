from __future__ import annotations

from datetime import UTC, datetime


def utc_now() -> str:
    """返回事件和记忆记录使用的 UTC 时间戳。"""

    return datetime.now(UTC).isoformat().replace("+00:00", "Z")
