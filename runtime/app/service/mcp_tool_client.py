from __future__ import annotations

from datetime import UTC, datetime, timedelta, timezone


def call_mcp_local_time(arguments: dict) -> dict:
    """阶段 2 的 MCP tool 本地适配实现。

    这里不直接引入 MCP SDK，是为了先固定 Runtime 内部的 MCP tool 调用边界。
    后续接入真实 MCP server 时，只需要把该函数替换为 SDK client 调用，并保持输出结构稳定。
    """

    timezone_name = arguments.get("timezone") or "Asia/Shanghai"
    # Windows 本机环境可能没有 IANA tzdata。阶段 2 demo 只承诺 Asia/Shanghai，
    # 因此用固定 UTC+8 偏移避免新增依赖；真实 MCP server 接入后再由外部服务处理时区。
    tz = timezone(timedelta(hours=8)) if timezone_name == "Asia/Shanghai" else UTC
    now = datetime.now(tz)
    return {
        "timezone": timezone_name,
        "currentTime": now.isoformat(),
    }
