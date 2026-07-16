package com.agentplatform.control.agent.dto;

import java.util.List;

/**
 * 单次运行使用的 Agent 配置快照。
 *
 * <p>控制面会在创建运行时固化该快照，避免运行过程中 Agent 配置变化影响审计复现。
 * 阶段 1 先提供本地开发默认值，后续再接入可配置的 Agent 注册表。</p>
 */
public record AgentSnapshot(
        String agentId,
        String agentVersion,
        String name,
        String systemPrompt,
        ModelConfig model,
        List<String> enabledTools,
        List<String> enabledSkills
) {
    /**
     * 构造阶段 1 的默认 Agent 快照。
     *
     * <p>该默认值让本机演示不依赖数据库或模型凭据，同时保留后续真实配置所需的字段。</p>
     */
    public static AgentSnapshot phaseOneDefault(String agentId) {
        return new AgentSnapshot(
                agentId,
                "1.0.0",
                "企业通用助手",
                "你是企业内部通用助手。阶段 1 仅允许本地开发模型、短期记忆和基础审计。",
                new ModelConfig("local-dev", "deterministic-phase1", 0.0),
                List.of(),
                List.of()
        );
    }
}
