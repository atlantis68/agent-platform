package com.agentplatform.control.agent.dto;

/**
 * 从审计事件派生的轻量用量计数。
 *
 * <p>阶段 2 开始，{@code toolCalls} 由 {@code tool.completed} 事件派生。
 * 这样控制面不需要信任 Runtime 额外上报的计数字段，审计事件就是统计来源。</p>
 */
public record RunUsage(
        long modelRequests,
        long outputEvents,
        long toolCalls
) {
}
