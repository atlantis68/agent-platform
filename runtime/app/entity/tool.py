from __future__ import annotations

from typing import Any, Literal

from pydantic import BaseModel


class ToolDefinition(BaseModel):
    """Runtime 内部使用的工具定义。

    工具定义只保存执行所需的非敏感元数据。真实外部地址、密钥和审批配置后续应由
    控制面下发或从安全配置读取，不能硬编码在 demo 注册表里。
    """

    name: str
    display_name: str
    description: str
    source_type: Literal["http", "mcp"]
    risk_level: Literal["LOW", "MEDIUM", "HIGH"]
    input_schema: dict[str, Any]


class ToolResult(BaseModel):
    """一次工具调用的结构化结果。"""

    tool_name: str
    source_type: Literal["http", "mcp"]
    risk_level: Literal["LOW", "MEDIUM", "HIGH"]
    output: dict[str, Any]
