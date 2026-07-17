package com.agentplatform.control.agent.controller;

import com.agentplatform.control.ControlPlaneApplication;
import com.agentplatform.control.agent.client.RuntimeClient;
import com.agentplatform.control.agent.dto.RunEvent;
import com.agentplatform.control.agent.dto.RuntimeAcceptedResponse;
import com.agentplatform.control.agent.dto.RuntimeRunRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ControlPlaneApplication.class)
@AutoConfigureMockMvc
class AgentRunControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RuntimeClient runtimeClient;

    @Test
    void createRunSubmitsRuntimeRequestAndReturnsEventStreamUrl() throws Exception {
        when(runtimeClient.submitRun(any(RuntimeRunRequest.class)))
                .thenReturn(new RuntimeAcceptedResponse("run_mocked", "accepted"));
        when(runtimeClient.fetchEvents(any(String.class)))
                .thenReturn(List.of(
                        new RunEvent("evt_1", "tr_mocked", "run_mocked", "run.started", 1, "2026-07-15T09:00:00Z", Map.of()),
                        new RunEvent("evt_2", "tr_mocked", "run_mocked", "run.completed", 2, "2026-07-15T09:00:01Z", Map.of("status", "completed"))
                ));

        mockMvc.perform(post("/api/v1/agent-runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "agentId": "agent_general_001",
                                  "input": {"type": "text", "text": "你好"},
                                  "context": {"conversationId": "conv_001", "source": "test"}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.runId").exists())
                .andExpect(jsonPath("$.status").value("queued"))
                .andExpect(jsonPath("$.eventStreamUrl", containsString("/api/v1/agent-runs/")));
    }

    @Test
    void auditEventsExposeRuntimeEventsInSequence() throws Exception {
        when(runtimeClient.submitRun(any(RuntimeRunRequest.class)))
                .thenReturn(new RuntimeAcceptedResponse("run_mocked", "accepted"));
        when(runtimeClient.fetchEvents(any(String.class)))
                .thenReturn(List.of(
                        new RunEvent("evt_1", "tr_mocked", "run_mocked", "run.started", 1, "2026-07-15T09:00:00Z", Map.of()),
                        new RunEvent("evt_2", "tr_mocked", "run_mocked", "run.completed", 2, "2026-07-15T09:00:01Z", Map.of("status", "completed"))
                ));

        String response = mockMvc.perform(post("/api/v1/agent-runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "agentId": "agent_general_001",
                                  "input": {"type": "text", "text": "验收"},
                                  "context": {"conversationId": "conv_audit", "source": "test"}
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String runId = response.replaceAll(".*\\\"runId\\\":\\\"([^\\\"]+)\\\".*", "$1");

        mockMvc.perform(get("/api/v1/agent-runs/" + runId + "/audit-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events[0].type").value("run.started"))
                .andExpect(jsonPath("$.events[1].type").value("run.completed"));
    }

    @Test
    void runSummaryCountsCompletedToolEvents() throws Exception {
        when(runtimeClient.submitRun(any(RuntimeRunRequest.class)))
                .thenReturn(new RuntimeAcceptedResponse("run_mocked", "accepted"));
        when(runtimeClient.fetchEvents(any(String.class)))
                .thenReturn(List.of(
                        new RunEvent("evt_1", "tr_mocked", "run_mocked", "tool.completed", 1,
                                "2026-07-16T09:00:00Z", Map.of("toolName", "http_echo")),
                        new RunEvent("evt_2", "tr_mocked", "run_mocked", "run.completed", 2,
                                "2026-07-16T09:00:01Z", Map.of("status", "completed"))
                ));

        String response = mockMvc.perform(post("/api/v1/agent-runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "agentId": "agent_general_001",
                                  "input": {"type": "text", "text": "工具统计"},
                                  "context": {"conversationId": "conv_tool_usage", "source": "test"}
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String runId = response.replaceAll(".*\\\"runId\\\":\\\"([^\\\"]+)\\\".*", "$1");

        mockMvc.perform(get("/api/v1/agent-runs/" + runId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usage.toolCalls").value(1));
    }

    @Test
    void eventStreamUsesUtf8ForChinesePayload() throws Exception {
        when(runtimeClient.submitRun(any(RuntimeRunRequest.class)))
                .thenReturn(new RuntimeAcceptedResponse("run_mocked", "accepted"));
        when(runtimeClient.fetchEvents(any(String.class)))
                .thenReturn(List.of(
                        new RunEvent("evt_1", "tr_mocked", "run_mocked", "run.output.delta", 1, "2026-07-15T09:00:00Z",
                                Map.of("text", "【本地开发模型】收到：中文验收")),
                        new RunEvent("evt_2", "tr_mocked", "run_mocked", "run.completed", 2, "2026-07-15T09:00:01Z",
                                Map.of("status", "completed"))
                ));

        String response = mockMvc.perform(post("/api/v1/agent-runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "agentId": "agent_general_001",
                                  "input": {"type": "text", "text": "中文验收"},
                                  "context": {"conversationId": "conv_sse_utf8", "source": "test"}
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String runId = response.replaceAll(".*\\\"runId\\\":\\\"([^\\\"]+)\\\".*", "$1");

        mockMvc.perform(get("/api/v1/agent-runs/" + runId + "/events"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("charset=UTF-8")))
                .andExpect(content().string(containsString("【本地开发模型】收到：中文验收")));
    }

    @Test
    void staticConsoleIsAvailableForPhaseOneShowcase() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Agent Platform")));
    }
}
