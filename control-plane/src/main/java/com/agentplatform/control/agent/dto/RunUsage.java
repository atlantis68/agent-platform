package com.agentplatform.control.agent.dto;

/**
 * 从审计事件派生的轻量用量计数。
 *
 * <p>阶段 1 中 {@code toolCalls} 始终为 0，因为工具和 MCP 执行明确推迟到阶段 2。</p>
 */
public record RunUsage(
        long modelRequests,
        long outputEvents,
        long toolCalls
) {
}
