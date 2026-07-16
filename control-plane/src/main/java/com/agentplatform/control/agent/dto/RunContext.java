package com.agentplatform.control.agent.dto;

/**
 * 不直接进入提示词的执行上下文。
 *
 * <p>{@code conversationId} 是与 Python Runtime 共享的短期记忆键。
 * 默认值在边界层补齐，让下游代码可以假设会话和来源字段始终稳定。</p>
 */
public record RunContext(
        String conversationId,
        String source
) {
    public RunContext {
        if (conversationId == null || conversationId.isBlank()) {
            conversationId = "default";
        }
        if (source == null || source.isBlank()) {
            source = "api";
        }
    }
}
