package com.agentplatform.control.agent.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 单次 Agent Run 面向用户的输入。
 *
 * <p>阶段 1 只接受文本输入，但显式保留 {@code type} 字段。
 * 这样后续支持多模态输入时，可以沿用同一层协议外壳。</p>
 */
public record RunInput(
        @NotBlank String type,
        @NotBlank String text
) {
}
