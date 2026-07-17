package com.agentplatform.control.agent.dto;

import java.util.Map;

/**
 * 控制面对外展示的工具定义。
 *
 * <p>该 DTO 只描述工具元数据，不包含真实鉴权凭据或外部连接配置。
 * 这样前端、审计和 Runtime 都能基于同一份工具契约工作，同时避免把敏感配置暴露给调用方。</p>
 */
public record ToolDefinition(
        String name,
        String displayName,
        String description,
        String sourceType,
        ToolRiskLevel riskLevel,
        Map<String, Object> inputSchema
) {
}
