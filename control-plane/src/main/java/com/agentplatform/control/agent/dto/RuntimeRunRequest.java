package com.agentplatform.control.agent.dto;

/**
 * Java 控制面发送给 Python Runtime 的内部运行请求。
 *
 * <p>该对象不会直接暴露给浏览器客户端。它会固化单次运行所需的 Agent 快照、
 * 追踪标识、输入和短期记忆上下文，确保 Runtime 执行过程可审计。</p>
 */
public record RuntimeRunRequest(
        String traceId,
        String runId,
        String tenantId,
        String userId,
        AgentSnapshot agentSnapshot,
        RunInput input,
        RunContext context
) {
}
