package com.agentplatform.control.agent.entity;

import com.agentplatform.control.agent.dto.AgentRunSummary;
import com.agentplatform.control.agent.dto.RunEvent;
import com.agentplatform.control.agent.dto.RunUsage;
import java.util.List;

/**
 * 阶段 1 控制面持有的可变内存运行记录。
 *
 * <p>这里把可变状态限制在本包内，是因为阶段 1 还没有数据库。
 * 后续持久化阶段应使用仓储支撑的聚合替换该类，避免把可变状态泄露给控制器。</p>
 */
public class StoredRun {

    private final String traceId;
    private final String runId;
    private final String agentId;
    private final String agentVersion;
    private String status;
    private final String startedAt;
    private String completedAt;
    private final List<RunEvent> auditEvents;

    public StoredRun(
            String traceId,
            String runId,
            String agentId,
            String agentVersion,
            String status,
            String startedAt,
            String completedAt,
            List<RunEvent> auditEvents
    ) {
        this.traceId = traceId;
        this.runId = runId;
        this.agentId = agentId;
        this.agentVersion = agentVersion;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.auditEvents = auditEvents;
    }

    public List<RunEvent> auditEvents() {
        return auditEvents;
    }

    public void status(String status) {
        this.status = status;
    }

    public void completedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    public AgentRunSummary toSummary() {
        long modelRequests = auditEvents.stream()
                .filter(event -> "model.requested".equals(event.type()))
                .count();
        long outputEvents = auditEvents.stream()
                .filter(event -> "run.output.delta".equals(event.type()))
                .count();
        long toolCalls = auditEvents.stream()
                .filter(event -> "tool.completed".equals(event.type()))
                .count();
        return new AgentRunSummary(
                traceId,
                runId,
                status,
                agentId,
                agentVersion,
                startedAt,
                completedAt,
                new RunUsage(modelRequests, outputEvents, toolCalls)
        );
    }
}
