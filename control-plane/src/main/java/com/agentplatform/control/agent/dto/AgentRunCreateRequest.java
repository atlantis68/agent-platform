package com.agentplatform.control.agent.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 前端创建 Agent Run 时提交的公开请求。
 *
 * <p>阶段 1 只暴露 Agent 标识、文本输入和可选上下文。
 * 幂等键先进入协议但暂不生效，后续持久化阶段再补齐去重语义。</p>
 */
public record AgentRunCreateRequest(
        @NotBlank String agentId,
        @Valid @NotNull RunInput input,
        @Valid RunContext context,
        String idempotencyKey
) {
    /**
     * 返回可安全传递给 Runtime 的运行上下文。
     *
     * <p>前端可以省略上下文，此处补齐默认值，避免 Runtime 侧出现空会话键。</p>
     */
    public RunContext contextOrDefault() {
        return context == null ? new RunContext("default", "api") : context;
    }
}
