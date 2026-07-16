package com.agentplatform.control.agent.client;

import com.agentplatform.control.agent.dto.RunEvent;
import com.agentplatform.control.agent.dto.RuntimeAcceptedResponse;
import com.agentplatform.control.agent.dto.RuntimeRunRequest;
import java.util.List;

/**
 * 控制面访问 Python Runtime 的边界接口。
 *
 * <p>测试会用 mock 替换该接口，阶段 1 生产链路使用 {@link HttpRuntimeClient}。
 * 保持边界足够小，可以降低后续切换到 MQ、gRPC 或持久化工作流分发时的改造范围。</p>
 */
public interface RuntimeClient {

    RuntimeAcceptedResponse submitRun(RuntimeRunRequest request);

    List<RunEvent> fetchEvents(String runId);
}
