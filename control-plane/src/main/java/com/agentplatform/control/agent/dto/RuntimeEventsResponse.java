package com.agentplatform.control.agent.dto;

import java.util.List;

/**
 * Runtime 内部接口返回的单次运行有序事件列表。
 */
public record RuntimeEventsResponse(
        List<RunEvent> events
) {
}
