package com.agentplatform.control.agent.dto;

/**
 * 控制面接受 Agent Run 后返回给前端的公开响应。
 *
 * <p>本地 Runtime 可能很快完成执行，但 API 仍先返回 {@code queued}。
 * 这样要求客户端通过 SSE URL 和审计接口消费进度，而不是假设运行会同步完成。</p>
 */
public record AgentRunCreateResponse(
        String traceId,
        String runId,
        String status,
        String eventStreamUrl
) {
}
