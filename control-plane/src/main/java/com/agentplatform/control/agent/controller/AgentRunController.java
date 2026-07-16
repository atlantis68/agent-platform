package com.agentplatform.control.agent.controller;

import com.agentplatform.control.agent.dto.AgentRunCreateRequest;
import com.agentplatform.control.agent.dto.AgentRunCreateResponse;
import com.agentplatform.control.agent.dto.AgentRunSummary;
import com.agentplatform.control.agent.dto.AuditEventsResponse;
import com.agentplatform.control.agent.dto.RunEvent;
import com.agentplatform.control.agent.service.AgentRunService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/agent-runs")
public class AgentRunController {

    private static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);

    private final AgentRunService agentRunService;

    public AgentRunController(AgentRunService agentRunService) {
        this.agentRunService = agentRunService;
    }

    @PostMapping
    public AgentRunCreateResponse createRun(@Valid @RequestBody AgentRunCreateRequest request) {
        return agentRunService.createRun(request);
    }

    @GetMapping("/{runId}")
    public AgentRunSummary getRun(@PathVariable String runId) {
        return agentRunService.getRun(runId);
    }

    @GetMapping("/{runId}/audit-events")
    public AuditEventsResponse getAuditEvents(@PathVariable String runId) {
        return agentRunService.getAuditEvents(runId);
    }

    @GetMapping(value = "/{runId}/events", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter streamEvents(@PathVariable String runId, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/event-stream;charset=UTF-8");

        SseEmitter emitter = new SseEmitter(30_000L);
        for (RunEvent event : agentRunService.refreshAndGetEvents(runId)) {
            // 事件名沿用 Runtime 的 `type` 字段，方便浏览器客户端订阅全量事件或特定生命周期事件。
            emitter.send(SseEmitter.event().name(event.type()).data(event, APPLICATION_JSON_UTF8));
        }
        emitter.complete();
        return emitter;
    }
}
