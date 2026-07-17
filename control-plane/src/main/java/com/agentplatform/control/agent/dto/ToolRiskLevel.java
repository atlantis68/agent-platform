package com.agentplatform.control.agent.dto;

/**
 * 工具风险等级。
 *
 * <p>阶段 2 只默认开放 LOW 风险工具。MEDIUM 和 HIGH 先进入元数据模型，
 * 是为了让后续审批、RBAC 和 Guardrail 能在同一个字段上扩展，不需要再破坏接口。</p>
 */
public enum ToolRiskLevel {
    LOW,
    MEDIUM,
    HIGH
}
