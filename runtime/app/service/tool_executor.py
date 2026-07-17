from __future__ import annotations

from app.entity.tool import ToolDefinition, ToolResult
from app.service.mcp_tool_client import call_mcp_local_time


def execute_tool(tool: ToolDefinition, arguments: dict) -> ToolResult:
    """执行一个低风险 demo 工具。

    阶段 2 的安全边界是只执行 LOW 风险工具。MEDIUM/HIGH 即使进入注册表，
    也必须等治理和审批阶段完成后才能开放。
    """

    if tool.risk_level != "LOW":
        raise ValueError(f"工具 {tool.name} 风险等级不是 LOW，阶段 2 默认拒绝执行")

    if tool.name == "http_echo":
        text = str(arguments.get("text", ""))
        output = {
            "text": text,
            "length": len(text),
        }
    elif tool.name == "mcp_local_time":
        output = call_mcp_local_time(arguments)
    else:
        raise ValueError(f"未知工具：{tool.name}")

    return ToolResult(
        tool_name=tool.name,
        source_type=tool.source_type,
        risk_level=tool.risk_level,
        output=output,
    )
