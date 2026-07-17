from __future__ import annotations

from typing import Any

from app.entity.tool import ToolDefinition


def list_tools() -> list[ToolDefinition]:
    """返回阶段 2 Runtime 可执行的 demo 工具。

    这里与控制面的 Tool Registry 保持同名工具，后续替换为真实注册中心时，
    Runtime 可以继续沿用 `name/source_type/risk_level/input_schema` 这些稳定字段。
    """

    return [
        ToolDefinition(
            name="http_echo",
            display_name="HTTP 回显工具",
            description="回显输入文本并返回字符长度，用于验证 HTTP 工具调用链路。",
            source_type="http",
            risk_level="LOW",
            input_schema={
                "type": "object",
                "properties": {
                    "text": {
                        "type": "string",
                        "description": "需要回显的文本",
                    }
                },
                "required": ["text"],
            },
        ),
        ToolDefinition(
            name="mcp_local_time",
            display_name="MCP 本地时间工具",
            description="返回指定时区的当前时间，用于验证 MCP tool 调用抽象。",
            source_type="mcp",
            risk_level="LOW",
            input_schema={
                "type": "object",
                "properties": {
                    "timezone": {
                        "type": "string",
                        "description": "IANA 时区名称，例如 Asia/Shanghai",
                        "default": "Asia/Shanghai",
                    }
                },
            },
        ),
    ]


def get_tool(tool_name: str) -> ToolDefinition | None:
    """按工具名查找 Runtime 本地工具定义。"""

    return next((tool for tool in list_tools() if tool.name == tool_name), None)


def build_tool_arguments(tool_name: str, input_text: str) -> dict[str, Any]:
    """根据阶段 2 demo 触发规则构造工具入参。

    当前阶段先使用确定性规则，避免在还没有真实模型规划工具调用时引入不可控行为。
    后续模型网关具备 function calling 后，可以把这里替换为模型输出的工具调用参数。
    """

    if tool_name == "http_echo":
        return {"text": input_text}
    if tool_name == "mcp_local_time":
        return {"timezone": "Asia/Shanghai"}
    return {}
