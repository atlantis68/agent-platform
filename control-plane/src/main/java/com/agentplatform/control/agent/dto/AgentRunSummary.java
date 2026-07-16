package com.agentplatform.control.agent.dto;

/**
 * Agent Run 查询接口返回的摘要视图。
 *
 * <p>阶段 1 有意从内存状态派生该摘要。
 * 后续阶段可以替换底层存储，而不改变返回给前端控制台的 API 形状。</p>
 */
public record AgentRunSummary(
        String traceId,
        String runId,
        String status,
        String agentId,
        String agentVersion,
        String startedAt,
        String completedAt,
        RunUsage usage
) {
}
