package com.agentplatform.control.agent.client;

import com.agentplatform.control.agent.dto.AgentSnapshot;
import com.agentplatform.control.agent.dto.ModelConfig;
import com.agentplatform.control.agent.dto.RunContext;
import com.agentplatform.control.agent.dto.RunInput;
import com.agentplatform.control.agent.dto.RuntimeAcceptedResponse;
import com.agentplatform.control.agent.dto.RuntimeRunRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HttpRuntimeClientTest {

    @Test
    void submitRunSendsJsonBodyToRuntime() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        HttpRuntimeClient client = new HttpRuntimeClient(builder.baseUrl("http://runtime.local").build());

        server.expect(requestTo("http://runtime.local/internal/v1/runs"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, containsString(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(content().string(containsString("\"runId\":\"run_http\"")))
                .andExpect(content().string(containsString("\"type\":\"text\"")))
                .andExpect(content().string(containsString("\"conversationId\":\"conv_http\"")))
                .andRespond(withSuccess(
                        "{\"runId\":\"run_http\",\"status\":\"accepted\"}",
                        MediaType.APPLICATION_JSON
                ));

        RuntimeAcceptedResponse response = client.submitRun(buildRuntimeRunRequest());

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("accepted");
        server.verify();
    }

    private RuntimeRunRequest buildRuntimeRunRequest() {
        return new RuntimeRunRequest(
                "tr_http",
                "run_http",
                "tenant_default",
                "user_001",
                new AgentSnapshot(
                        "agent_general_001",
                        "1.0.0",
                        "企业通用助手",
                        "阶段 1 HTTP 适配器测试。",
                        new ModelConfig("local-dev", "deterministic-phase1", 0.0),
                        List.of(),
                        List.of()
                ),
                new RunInput("text", "hello"),
                new RunContext("conv_http", "test")
        );
    }
}
