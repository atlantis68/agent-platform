package com.agentplatform.control.agent.dto;

import java.util.Map;

/**
 * Runtime 产生的审计事件和流式事件。
 *
 * <p>字段名刻意与阶段 0 Runtime 协议保持一致。
 * 控制面会把这些记录作为审计证据保存，也会通过 SSE 转发，因此事件顺序必须稳定且有序。</p>
 */
public record RunEvent(
        String eventId,
        String traceId,
        String runId,
        String type,
        int sequence,
        String timestamp,
        Map<String, Object> payload
) {
}
