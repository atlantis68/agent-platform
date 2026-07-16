package com.agentplatform.control.agent.dto;

import java.util.List;

/**
 * 控制面对外暴露的公开审计响应。
 *
 * <p>阶段 1 中该响应直接镜像 Runtime 事件。
 * 后续阶段可以增加治理相关的富化字段，而不改变事件列表契约。</p>
 */
public record AuditEventsResponse(
        List<RunEvent> events
) {
}
