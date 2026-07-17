package com.agentplatform.control.agent.controller;

import com.agentplatform.control.agent.dto.ToolDefinition;
import com.agentplatform.control.agent.service.ToolRegistryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tools")
public class ToolRegistryController {

    private final ToolRegistryService toolRegistryService;

    public ToolRegistryController(ToolRegistryService toolRegistryService) {
        this.toolRegistryService = toolRegistryService;
    }

    @GetMapping
    public ToolRegistryResponse listTools() {
        return new ToolRegistryResponse(toolRegistryService.listTools());
    }

    /**
     * 工具列表响应包装。
     *
     * <p>使用根节点 `tools` 包装列表，是为了后续可以兼容追加分页、版本号或注册表来源信息。</p>
     */
    public record ToolRegistryResponse(List<ToolDefinition> tools) {
    }
}
