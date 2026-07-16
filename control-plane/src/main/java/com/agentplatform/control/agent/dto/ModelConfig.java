package com.agentplatform.control.agent.dto;

/**
 * 不可变 Agent 快照中固化的模型选择。
 *
 * <p>阶段 1 只使用 {@code local-dev} 供应商。
 * 现在把这些字段纳入协议，是为了避免后续模型网关接入时产生破坏性 DTO 变更。</p>
 */
public record ModelConfig(
        String provider,
        String modelName,
        double temperature
) {
}
