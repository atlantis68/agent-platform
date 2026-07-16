from fastapi.testclient import TestClient

from app.main import app


def build_runtime_run(run_id: str, conversation_id: str, text: str) -> dict:
    """构造控制面协议使用的最小 Runtime 请求。

    这里刻意复用阶段 0 协议字段，让后续实现者在接入真实模型或工具之前，
    就能看到可追踪运行所必须携带的标识。
    """

    return {
        "traceId": f"tr_{run_id}",
        "runId": run_id,
        "tenantId": "tenant_default",
        "userId": "user_001",
        "agentSnapshot": {
            "agentId": "agent_general_001",
            "agentVersion": "1.0.0",
            "name": "企业通用助手",
            "systemPrompt": "你是企业内部通用助手。",
            "model": {
                "provider": "local-dev",
                "modelName": "deterministic-phase1",
                "temperature": 0.0,
            },
            "enabledTools": [],
            "enabledSkills": [],
        },
        "input": {
            "type": "text",
            "text": text,
        },
        "context": {
            "conversationId": conversation_id,
            "source": "test",
        },
    }


def test_run_records_events_and_short_term_memory():
    client = TestClient(app)

    payload = build_runtime_run(
        run_id="run_001",
        conversation_id="conv_001",
        text="第一轮",
    )

    response = client.post("/internal/v1/runs", json=payload)

    assert response.status_code == 200
    assert response.json() == {"runId": "run_001", "status": "accepted"}

    events = client.get("/internal/v1/runs/run_001/events").json()["events"]
    assert [event["type"] for event in events] == [
        "run.started",
        "model.requested",
        "run.output.delta",
        "model.completed",
        "run.completed",
    ]
    assert all(event["traceId"] == "tr_run_001" for event in events)
    assert all(event["runId"] == "run_001" for event in events)
    assert [event["sequence"] for event in events] == [1, 2, 3, 4, 5]

    memory = client.get("/internal/v1/conversations/conv_001/messages").json()[
        "messages"
    ]
    assert [message["role"] for message in memory] == ["user", "assistant"]
    assert memory[0]["content"] == "第一轮"
    assert "第 1 轮" in memory[1]["content"]


def test_second_run_uses_same_conversation_memory_window():
    client = TestClient(app)

    first = build_runtime_run("run_101", "conv_memory", "第一轮")
    second = build_runtime_run("run_102", "conv_memory", "第二轮")

    assert client.post("/internal/v1/runs", json=first).status_code == 200
    assert client.post("/internal/v1/runs", json=second).status_code == 200

    memory = client.get("/internal/v1/conversations/conv_memory/messages").json()[
        "messages"
    ]

    assert [message["role"] for message in memory] == [
        "user",
        "assistant",
        "user",
        "assistant",
    ]
    assert "第 2 轮" in memory[-1]["content"]
