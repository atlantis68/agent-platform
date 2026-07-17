package com.agentplatform.control.agent.service;

import com.agentplatform.control.agent.dto.ToolDefinition;
import com.agentplatform.control.agent.dto.ToolRiskLevel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 阶段 2 的内存工具注册表。
 *
 * <p>先用固定 demo 工具建立协议闭环，原因是工具执行会牵涉权限、审计和外部系统副作用。
 * 在没有治理能力前，注册表只暴露 LOW 风险只读/本地工具，后续再替换为数据库和真实 MCP server 发现结果。</p>
 */
@Service
public class ToolRegistryService {

    public List<ToolDefinition> listTools() {
        return List.of(
                new ToolDefinition(
                        "http_echo",
                        "HTTP 回显工具",
                        "回显输入文本并返回字符长度，用于验证 HTTP 工具调用链路。",
                        "http",
                        ToolRiskLevel.LOW,
                        Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "text", Map.of(
                                                "type", "string",
                                                "description", "需要回显的文本"
                                        )
                                ),
                                "required", List.of("text")
                        )
                ),
                new ToolDefinition(
                        "mcp_local_time",
                        "MCP 本地时间工具",
                        "返回指定时区的当前时间，用于验证 MCP tool 调用抽象和审计链路。",
                        "mcp",
                        ToolRiskLevel.LOW,
                        Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "timezone", Map.of(
                                                "type", "string",
                                                "description", "IANA 时区名称，例如 Asia/Shanghai",
                                                "default", "Asia/Shanghai"
                                        )
                                )
                        )
                )
        );
    }
}
