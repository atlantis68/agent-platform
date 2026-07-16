from fastapi import FastAPI

from app.controller.runtime_controller import router as runtime_router

app = FastAPI(
    title="Agent Platform Runtime",
    version="0.1.0-phase1",
    description=(
        "阶段 1 本地 Agent Runtime。这里有意使用内存存储和本地开发模型，"
        "让平台运行闭环不依赖外部模型凭据也能完成验证。"
    ),
)

app.include_router(runtime_router)
