package com.agentplatform.control.agent.dto;

/**
 * Python Runtime 接受运行请求后返回给控制面的内部响应。
 */
public record RuntimeAcceptedResponse(
        String runId,
        String status
) {
}
