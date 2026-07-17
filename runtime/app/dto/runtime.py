from __future__ import annotations

from typing import Literal

from pydantic import BaseModel, Field


class ModelConfig(BaseModel):
    """控制面随 Agent 快照下发的模型配置。

    阶段 1 只支持 `local-dev` 本地开发模型。现在保留完整模型字段，是为了让
    后续接入真实模型网关时不需要破坏 Runtime 请求协议。
    """

    provider: str = "local-dev"
    modelName: str = "deterministic-phase1"
    temperature: float = 0.0


class AgentSnapshot(BaseModel):
    """单次运行使用的不可变 Agent 配置快照。"""

    agentId: str
    agentVersion: str = "1.0.0"
    name: str
    systemPrompt: str
    model: ModelConfig = Field(default_factory=ModelConfig)
    # enabledTools 是控制面授权给本次运行的工具白名单。Runtime 只能在该列表内选择工具，
    # 不能仅根据用户输入文本自行调用未启用工具。
    enabledTools: list[str] = Field(default_factory=list)
    enabledSkills: list[str] = Field(default_factory=list)


class RunInput(BaseModel):
    """控制面传入 Runtime 的用户输入。

    阶段 1 只接受文本输入。保留 `type` 字段是为了让后续多模态输入能沿用同一
    顶层协议结构扩展。
    """

    type: Literal["text"]
    text: str


class RunContext(BaseModel):
    """不直接进入用户提示词的运行上下文。"""

    conversationId: str = "default"
    source: str = "api"


class RuntimeRunRequest(BaseModel):
    """Java 控制面发送给 Python Runtime 的内部运行请求。"""

    traceId: str
    runId: str
    tenantId: str
    userId: str
    agentSnapshot: AgentSnapshot
    input: RunInput
    context: RunContext = Field(default_factory=RunContext)
