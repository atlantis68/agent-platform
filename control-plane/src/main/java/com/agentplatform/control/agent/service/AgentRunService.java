package com.agentplatform.control.agent.service;

import com.agentplatform.control.agent.client.RuntimeClient;
import com.agentplatform.control.agent.dto.AgentRunCreateRequest;
import com.agentplatform.control.agent.dto.AgentRunCreateResponse;
import com.agentplatform.control.agent.dto.AgentRunSummary;
import com.agentplatform.control.agent.dto.AgentSnapshot;
import com.agentplatform.control.agent.dto.AuditEventsResponse;
import com.agentplatform.control.agent.dto.RunContext;
import com.agentplatform.control.agent.dto.RunEvent;
import com.agentplatform.control.agent.dto.RuntimeRunRequest;
import com.agentplatform.control.agent.entity.StoredRun;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class AgentRunService {

    private final RuntimeClient runtimeClient;
    private final Map<String, StoredRun> runs = new ConcurrentHashMap<>();

    public AgentRunService(RuntimeClient runtimeClient) {
        this.runtimeClient = runtimeClient;
    }

    /**
     * 创建一次 Agent Run，并提交给本地 Runtime。
     *
     * <p>即使阶段 1 Runtime 会同步完成执行，对外响应仍先保持 queued。
     * 这样可以保留后续持久化工作流所需的异步运行模型。</p>
     */
    public AgentRunCreateResponse createRun(AgentRunCreateRequest request) {
        String runId = "run_" + UUID.randomUUID().toString().replace("-", "");
        String traceId = "tr_" + UUID.randomUUID().toString().replace("-", "");
        RunContext context = request.contextOrDefault();
        AgentSnapshot snapshot = AgentSnapshot.phaseTwoDefault(request.agentId());

        RuntimeRunRequest runtimeRequest = new RuntimeRunRequest(
                traceId,
                runId,
                "tenant_default",
                "user_001",
                snapshot,
                request.input(),
                context
        );

        StoredRun run = new StoredRun(
                traceId,
                runId,
                request.agentId(),
                snapshot.agentVersion(),
                "queued",
                Instant.now().toString(),
                null,
                new ArrayList<>()
        );
        runs.put(runId, run);

        runtimeClient.submitRun(runtimeRequest);
        refreshAuditEvents(runId);

        return new AgentRunCreateResponse(
                traceId,
                runId,
                "queued",
                "/api/v1/agent-runs/" + runId + "/events"
        );
    }

    public AgentRunSummary getRun(String runId) {
        StoredRun run = requireRun(runId);
        refreshAuditEvents(runId);
        return run.toSummary();
    }

    public AuditEventsResponse getAuditEvents(String runId) {
        return new AuditEventsResponse(refreshAndGetEvents(runId));
    }

    public List<RunEvent> refreshAndGetEvents(String runId) {
        StoredRun run = requireRun(runId);
        refreshAuditEvents(runId);
        return List.copyOf(run.auditEvents());
    }

    private void refreshAuditEvents(String runId) {
        StoredRun run = requireRun(runId);
        List<RunEvent> events = runtimeClient.fetchEvents(runId);
        run.auditEvents().clear();
        run.auditEvents().addAll(events);
        if (events.stream().anyMatch(event -> "run.completed".equals(event.type()))) {
            run.status("completed");
            run.completedAt(Instant.now().toString());
        } else if (events.stream().anyMatch(event -> "run.failed".equals(event.type()))) {
            run.status("failed");
            run.completedAt(Instant.now().toString());
        } else if (events.stream().anyMatch(event -> "run.started".equals(event.type()))) {
            run.status("running");
        }
    }

    private StoredRun requireRun(String runId) {
        StoredRun run = runs.get(runId);
        if (run == null) {
            throw new ResponseStatusException(NOT_FOUND, "Agent run not found: " + runId);
        }
        return run;
    }
}
