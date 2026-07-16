package com.agentplatform.control.agent.client;

import com.agentplatform.control.agent.dto.RunEvent;
import com.agentplatform.control.agent.dto.RuntimeAcceptedResponse;
import com.agentplatform.control.agent.dto.RuntimeEventsResponse;
import com.agentplatform.control.agent.dto.RuntimeRunRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.util.List;

@Component
public class HttpRuntimeClient implements RuntimeClient {

    private final RestClient restClient;

    @Autowired
    public HttpRuntimeClient(
            RestClient.Builder builder,
            @Value("${agent.runtime.base-url:http://127.0.0.1:8001}") String runtimeBaseUrl
    ) {
        // 基础 URL 可配置，便于本机开发时把 Runtime 放在不同端口而不改 Java 代码。
        // JDK HTTP Client 固定为 HTTP/1.1，是因为阶段 1 Uvicorn Runtime 不支持 h2c 升级请求。
        // 没有这个边界时，本机调用可能在 FastAPI 收到 JSON body 之前失败。
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.restClient = builder
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .baseUrl(runtimeBaseUrl)
                .build();
    }

    public HttpRuntimeClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public RuntimeAcceptedResponse submitRun(RuntimeRunRequest request) {
        return restClient.post()
                .uri("/internal/v1/runs")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(RuntimeAcceptedResponse.class);
    }

    @Override
    public List<RunEvent> fetchEvents(String runId) {
        RuntimeEventsResponse response = restClient.get()
                .uri("/internal/v1/runs/{runId}/events", runId)
                .retrieve()
                .body(RuntimeEventsResponse.class);
        return response == null ? List.of() : response.events();
    }
}
