# Progress

## 2026-07-15

- 创建项目目录：`D:\Workspace\agent-platform`。
- 建立分阶段落地计划，明确每阶段必须有成果展示与交付。
- 完成阶段 0 架构基线文档：
  - `docs/roadmap/phased-delivery-plan.md`
  - `docs/architecture/00-overview.md`
  - `docs/architecture/01-runtime-contracts.md`
  - `docs/delivery/phase-0-showcase.md`
  - `docs/superpowers/plans/2026-07-15-agent-platform-phase0.md`
- 完成占位符扫描，未发现计划缺陷关键词。
- 发现 `D:\Workspace\agent-platform` 尚未初始化 Git；根据用户最新约束，后续不执行 Git 写操作，除非用户重新授权。
- 用户明确要求当前项目暂时不用 Git 提交；已开始持久化项目规则并创建项目专属 Skill。
- 创建 `agent-platform-project-governance` Skill，并将项目规则和已知问题写入 Skill 问题日志。
- 首次运行 Skill 校验时遇到 Windows 默认编码问题，将改用 UTF-8 模式重新验证并记录到 Skill。
- 使用 `python -X utf8` 重新运行 Skill 校验，通过，输出 `Skill is valid!`。
- 项目目录和 Skill 目录占位符扫描均无命中；旧 Git 初始化/提交表述扫描无命中。
- 根据 Codex 官方说明，将项目专属 Skill 从用户级目录迁移到项目内 `.agents/skills/agent-platform-project-governance`。
- 2026-07-15 17:39：用户补充后续开发阶段交互规则：需要详细说明为什么这样做、做了什么；需要人工介入时及时停下来反馈并等待确认。
- 2026-07-15 17:47：开始阶段 1，范围限定为最小 Agent Runtime；本阶段不实现工具/MCP、RAG、长期记忆、Skill 运行时或 CLI。
- 2026-07-15 17:47：完成本机环境检查：Java 17、Maven 3.9.11、Python 3.12.10、pytest/httpx/uvicorn 可用，FastAPI 未安装，后续通过项目虚拟环境安装。
- 2026-07-15 18:05：阶段 1 复核时发现 Python Runtime 测试因系统 Python 缺少 FastAPI 失败；创建 `runtime\.venv` 并安装 `runtime\requirements-dev.txt`。
- 2026-07-15 18:08：Runtime 依赖初装后触发 FastAPI/Starlette TestClient 弃用警告；将 Runtime 依赖收窄到阶段 1 已验证次版本范围，`pip check` 通过，Runtime 测试 `2 passed`。
- 2026-07-15 18:12：补充 Java 协议 DTO、RuntimeClient 边界和运行服务注释，解释字段含义、阶段边界和后续替换点。
- 2026-07-15 18:12：控制面测试输出出现 JVM CDS 共享警告；为 Surefire 测试 JVM 配置 `-Xshare:off`，重新运行后警告消失。
- 2026-07-15 18:16：首次端到端验收中，PowerShell `Invoke-WebRequest` 访问页面出现客户端空引用；改用 `curl.exe` 验证页面 HTTP 200。
- 2026-07-15 18:16：Windows `curl.exe -d` 与 PowerShell 字符串转义导致 JSON 字段名双引号丢失，服务端返回 400；后续端到端验收改用 Python `httpx`。
- 2026-07-15 18:17：控制面调用 Runtime 时 Runtime 日志出现 `Unsupported upgrade request`，POST 返回 422，控制面返回 500；直接用 Python `httpx` 调 Runtime 成功，定位为 Java Runtime HTTP 客户端边界问题。
- 2026-07-15 18:20：修复 `HttpRuntimeClient`，固定到 HTTP/1.1，请求显式 `Content-Type: application/json`；新增 `HttpRuntimeClientTest` 覆盖 JSON 请求体。
- 2026-07-15 18:22：修复后控制面启动失败，原因是 `HttpRuntimeClient` 存在生产构造器和测试构造器，Spring 无法选择；新增 `ControlPlaneApplicationSmokeTest` 复现并用 `@Autowired` 标记生产构造器。
- 2026-07-15 18:24：阶段 1 自动化验证通过：`runtime\.venv\Scripts\python -m pytest runtime\tests -q` 为 `2 passed`；`mvn -f control-plane/pom.xml test` 为 `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`。
- 2026-07-15 18:24：阶段 1 端到端验收通过：运行台 HTTP 200，创建 Agent Run 返回 `queued`，运行摘要为 `completed`，审计事件 5 条，SSE 包含 `run.completed`，Runtime 短期记忆包含 `user,assistant`。
- 2026-07-15 18:25：创建阶段 1 成果展示文档 `docs/delivery/phase-1-showcase.md`，更新 README、计划状态和项目 Skill 问题日志；阶段 1 标记为 complete，未进入阶段 2。
- 2026-07-15 18:29：阶段 1 最终验证通过：`runtime\.venv\Scripts\python -m pip check` 输出 `No broken requirements found.`；`runtime\.venv\Scripts\python -m pytest runtime\tests -q` 输出 `2 passed`；`mvn -f control-plane/pom.xml test` 输出 `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`。
- 2026-07-15 18:30：停止阶段 1 本机验收用的 Runtime 和控制面进程；确认 8001、8080 无监听进程，端口已释放。

## 2026-07-16

- 2026-07-16 11:01：根据用户反馈，开始控制面结构治理；目标是将 `control-plane` 中原本平铺在 `agent` 包下的 controller、service、client、dto、entity 按类型拆分，不进入阶段 2。
- 2026-07-16 11:01：重构前运行 `mvn -f control-plane/pom.xml test` 建立基线，结果为 `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`。
- 2026-07-16 11:02：移动控制面源码到分层包：`agent.controller`、`agent.service`、`agent.client`、`agent.dto`、`agent.entity`；同步移动控制器测试和 Runtime Client 测试到对应测试包。
- 2026-07-16 11:04：机械替换脚本写法错误，导致 `AgentSnapshot.java`、`AgentRunCreateRequest.java` 出现字符级损坏，并且部分 Java 文件出现字面量 `\r\n`；已停止使用该脚本并改用 `apply_patch` 重建受影响文件。
- 2026-07-16 11:05：Windows PowerShell `Set-Content -Encoding UTF8` 写入 BOM，导致 javac 报 `\ufeff` 非法字符；改用 .NET `UTF8Encoding(false)` 重写 Java 文件为 UTF-8 无 BOM。
- 2026-07-16 11:06：分层后发现 `RunContext` 自定义 compact constructor 需要公开访问；将其改为 `public RunContext`。
- 2026-07-16 11:06：重构后运行 `mvn -f control-plane/pom.xml test`，结果为 `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`。
- 2026-07-16 11:07：结构自检通过：`control-plane/src/main/java/com/agentplatform/control/agent` 下仅保留 `client`、`controller`、`dto`、`entity`、`service` 五类目录；旧平铺包声明、损坏字符、字面量 `\r\n` 和 BOM 扫描均无命中。
- 2026-07-16 11:27：根据用户要求补充“所有代码备注统一使用中文”为项目全局规则，并同步更新 `AGENTS.md`、`task_plan.md` 和项目专属 Skill。
- 2026-07-16 11:27：将 Python Runtime 的英文 docstring、Java 控制面英文 JavaDoc/行注释改为中文；同时为 `AgentSnapshot`、`AgentRunCreateRequest` 补充中文协议边界说明。
- 2026-07-16 11:30：备注治理验证通过：旧英文备注短语扫描无命中；`runtime\.venv\Scripts\python -m pytest runtime\tests -q` 输出 `2 passed`；`mvn -f control-plane\pom.xml clean test` 强制重编译 19 个主源码和 3 个测试源码，最终 `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0` 且 `BUILD SUCCESS`。
- 2026-07-16 12:58：新增 Windows 本机启停脚本：`scripts/windows/start-python-runtime.ps1`、`stop-python-runtime.ps1`、`start-java-control-plane.ps1`、`stop-java-control-plane.ps1` 和公共进程工具 `_process-utils.ps1`；脚本使用 `.run/` 保存 PID 与日志，`.gitignore` 已忽略该运行态目录。
- 2026-07-16 12:58：脚本验证通过：5 个 `.ps1` 文件 PowerShell AST 语法解析通过；使用临时端口执行 `start-python-runtime.ps1 -CheckOnly -Port 18001` 和 `start-java-control-plane.ps1 -CheckOnly -Port 18080 -RuntimeBaseUrl http://127.0.0.1:18001` 均输出预期命令；`stop-python-runtime.ps1 -DryRun` 可识别当前 8001 Python Runtime；`stop-java-control-plane.ps1 -DryRun` 未误停 8080 上命令行特征不匹配的进程。
- 2026-07-16 15:56：修正停止脚本 `-DryRun` 提示文案，避免未实际停止进程时输出“已停止”造成误解；重新执行 AST、`-CheckOnly`、`-DryRun` 和 UTF-8 BOM 检查。
- 2026-07-16 15:59：修复控制面 SSE 事件中文乱码问题。根因是 `SseEmitter` 写出边界没有强制 UTF-8，浏览器直接打开 `/events` 时可能显示乱码；已在 `AgentRunController` 设置 `text/event-stream;charset=UTF-8` 和 JSON UTF-8 data 写出，并新增 `eventStreamUsesUtf8ForChinesePayload` 回归测试。
- 2026-07-16 15:59：乱码修复验证通过：新增回归测试先失败后通过；`runtime\.venv\Scripts\python -m pytest runtime\tests -q` 输出 `2 passed`；`mvn -f control-plane\pom.xml clean test` 输出 `Tests run: 6, Failures: 0, Errors: 0, Skipped: 0` 和 `BUILD SUCCESS`；临时新版控制面 `8003` 真实 HTTP 验证显示 SSE `Content-Type` 为 `text/event-stream;charset=UTF-8`、中文 payload `replacement_count=0`。
- 2026-07-16：用户确认阶段 1 改造已验收通过；已更新 README、阶段 1 成果展示、分阶段路线和任务计划，并创建阶段 2 工具系统与 MCP v1 实施计划 `docs/superpowers/plans/2026-07-16-agent-platform-phase2-tool-mcp.md`。阶段 2 仅进入 planned 状态，尚未开始编码。
- 2026-07-16 16:12：修复 Java 控制面停止脚本在 Spring Boot `@argfile` 启动场景下无法识别目标进程的问题。根因是监听端口进程命令行只包含 `-cp @spring-boot-*.argfile`，`control-plane` 实际在 argfile classpath 中；已让公共匹配函数读取 `.argfile` 内容参与匹配。验证 `stop-java-control-plane.ps1 -Port 8002 -DryRun` 输出将停止 PID 28980，且未实际停止进程。
- 2026-07-16 16:24：修复 Java 控制面启动脚本 Maven 参数传递问题。根因是 `Start-Process` 与 Maven/Spring Boot 三层解析会把 `--agent.runtime.base-url` 泄漏到 Maven 层或把逗号形式整体绑定到 `server.port`；改为启动前设置 `SERVER_PORT` 和 `AGENT_RUNTIME_BASE_URL` 环境变量，Maven 命令仅保留 `spring-boot:run`。验证临时端口 `18084` 启动成功，`/index.html` HTTP 200，停止脚本释放端口。
- 2026-07-16 17:10：用户确认本阶段改造已验证通过；本次只做归档和下一阶段规划，不启动阶段 2 编码。已补充 `README.md`、`task_plan.md`、`docs/roadmap/phased-delivery-plan.md` 和阶段 2 实施计划中的下一步说明，明确阶段 2 默认先用 `http_echo` 与 `mcp_local_time` demo 工具打通工具调用、审计和 SSE 展示闭环；真实 HTTP API、真实 MCP server、工具风险策略和新增 SDK 依赖授权均为可选信息，不提供也可按默认 demo 工具进入阶段 2。
- 2026-07-16：用户提供远程仓库 `https://github.com/atlantis68/agent-platform.git`，并明确授权将当前项目改动提交并推送到远程。已将 Git 写操作授权规则从“暂不执行”调整为“默认禁止，用户明确授权后可执行”，本次授权范围仅包含初始化仓库、提交当前项目和推送远程。
- 2026-07-16 17:18：提交前验证完成。`runtime\.venv\Scripts\python -m pytest runtime\tests -q` 输出 `2 passed`；`mvn -f control-plane\pom.xml clean test` 输出 `Tests run: 6, Failures: 0, Errors: 0, Skipped: 0` 和 `BUILD SUCCESS`；Windows 脚本 AST 检查首次因检查命令未初始化 `[ref]` 变量失败，修正命令后输出 `PowerShell AST OK`。
- 2026-07-16 17:25：完成本地 Git 初始化和首次提交 `3766087 feat(平台基线): 初始化 Agent 平台阶段 0-1`。推送到 GitHub 时，首次交互式 `git push -u origin main` 长时间无输出，定位为 Git Credential Manager 等待凭据；非交互重试返回 `could not read Username for 'https://github.com': terminal prompts disabled`；`gh` 未安装，SSH 探测返回 `Permission denied (publickey)`。远程推送当前阻塞于本机 GitHub 认证，需要用户完成凭据授权后继续。
