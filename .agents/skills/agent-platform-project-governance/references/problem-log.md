# Agent 平台问题与修复记录

本文件记录 `D:\Workspace\agent-platform` 项目过程中遇到的问题、根因、修复、验证和预防措施。后续每次发现问题都必须追加记录。

## 2026-07-15 17:10 - 初始项目目录不存在

- 阶段：阶段 0，架构与协议基线。
- 现象：工作区中存在多个项目目录，但没有明确的 `agent-platform` 项目入口。
- 影响：无法直接落地阶段计划和交付物，需要先创建独立项目目录。
- 根因：这是从零开始的新平台规划，尚未建立项目根目录。
- 修复：创建 `D:\Workspace\agent-platform`，并建立 `docs/architecture`、`docs/roadmap`、`docs/delivery`、`docs/superpowers/plans` 等目录。
- 验证：执行文件列表检查，确认阶段 0 的 10 个交付文件全部存在。
- 预防：后续所有项目文档、计划和阶段交付物都放在 `D:\Workspace\agent-platform`。
- 关联文件：`README.md`、`task_plan.md`、`progress.md`。

## 2026-07-15 17:14 - Git 初始化建议与用户新约束冲突

- 阶段：阶段 0，架构与协议基线。
- 现象：检查到 `D:\Workspace\agent-platform` 不是 Git 仓库，曾准备建议初始化仓库并提交阶段 0 基线。
- 影响：如果继续执行 Git 写操作，会违反用户后续明确要求“本机执行，暂时不用 Git 提交”。
- 根因：阶段 0 初期按照一般项目治理思路考虑 Git 提交；用户随后明确给出当前项目的长期约束。
- 修复：将“暂不 Git 提交”写入 `AGENTS.md`、`README.md`、`task_plan.md` 和本 Skill。后续只允许 Git 只读检查，不执行写操作。
- 验证：项目规则文件中已包含“不执行 Git 提交、推送、合并、打 tag”的硬约束。
- 预防：每次开始工作先读取 `AGENTS.md` 和本 Skill；用户未重新授权前不建议 Git 初始化或提交。
- 关联文件：`AGENTS.md`、`task_plan.md`、`progress.md`、`SKILL.md`。

## 2026-07-15 17:15 - 占位符扫描出现假阳性

- 阶段：阶段 0，架构与协议基线。
- 现象：使用 `rg` 扫描占位符时，命中了 `progress.md` 中直接复述扫描关键词的说明文字。
- 影响：扫描结果看起来像存在未完成占位符，降低验收信号质量。
- 根因：进度记录中直接写入了要扫描的关键词。
- 修复：将进度描述改为“未发现计划缺陷关键词”，避免机器扫描误报。
- 验证：重新运行占位符扫描，命令返回无命中。
- 预防：在日志或报告中避免直接写入会被扫描规则命中的关键词；必要时用描述性替代表达。
- 关联文件：`progress.md`。

## 2026-07-15 17:25 - Skill 初始化脚本因短描述过短失败

- 阶段：项目治理 Skill 创建。
- 现象：运行 `init_skill.py` 创建 `agent-platform-project-governance` 时，脚本报错 `short_description must be 25-64 characters`。
- 影响：技能目录和模板文件已生成，但 `agents/openai.yaml` 和资源目录没有完整生成。
- 根因：传入的 UI 短描述中文字符计数未满足脚本要求。
- 修复：保留已生成的技能目录，手动补全 `SKILL.md`、`references/problem-log.md` 和 `agents/openai.yaml`，再运行 `quick_validate.py` 校验。
- 验证：最终使用 `python -X utf8 quick_validate.py` 校验通过，输出 `Skill is valid!`。
- 预防：后续使用 `init_skill.py` 时，给 `short_description` 提供更长且明确的文本。
- 关联文件：`C:\Users\EDY\.codex\skills\agent-platform-project-governance\SKILL.md`。

## 2026-07-15 17:30 - Windows 下 Skill 校验脚本默认编码导致失败

- 阶段：项目治理 Skill 校验。
- 现象：运行 `quick_validate.py` 校验 Skill 时抛出 `UnicodeDecodeError`，错误信息显示 Python 使用系统默认编码读取 `SKILL.md`。
- 影响：技能内容本身未必有问题，但校验脚本无法在默认命令下读取中文 UTF-8 文档。
- 根因：Windows 环境下 Python `Path.read_text()` 未显式指定编码时可能使用本地默认编码，而本项目和 Skill 均按 UTF-8 编写。
- 修复：使用 `python -X utf8` 运行校验脚本，强制 Python 以 UTF-8 模式处理文本。
- 验证：重新运行 `python -X utf8 quick_validate.py` 后输出 `Skill is valid!`。
- 预防：后续在 Windows 上运行不显式指定编码的 Python 校验脚本时，优先使用 `python -X utf8`。
- 关联文件：`C:\Users\EDY\.codex\skills\.system\skill-creator\scripts\quick_validate.py`。

## 2026-07-15 17:40 - 项目专属 Skill 初始放置在用户级目录

- 阶段：项目治理 Skill 创建。
- 现象：`agent-platform-project-governance` 初始创建在 `C:\Users\EDY\.codex\skills`，用户追问为什么没有放到当前项目目录。
- 影响：用户级目录会让 Skill 看起来像个人全局能力，不符合“针对当前项目创建独立 Skill”的隔离预期；如果多个项目复用同名 Skill，还可能造成选择器里出现重复名称。
- 根因：创建时参考了当前会话实际可见的技能安装目录，优先保证当前 Codex 环境可发现；但官方说明中，项目适用的 repo skill 推荐放在项目 `.agents/skills`。
- 修复：将 Skill 迁移到 `D:\Workspace\agent-platform\.agents\skills\agent-platform-project-governance`，并更新 `AGENTS.md`、`task_plan.md`、`progress.md` 中的路径说明。
- 验证：确认项目内 Skill 的 `SKILL.md`、`agents/openai.yaml`、`references/problem-log.md` 均存在，并重新运行 Skill 校验。
- 预防：后续项目专属 Skill 优先创建在项目 `.agents/skills`；只有跨项目个人工作流才放用户级目录。
- 关联文件：`AGENTS.md`、`task_plan.md`、`progress.md`、`.agents/skills/agent-platform-project-governance/SKILL.md`。

## 2026-07-15 17:39 - 补充交互归档和人工介入规则

- 阶段：项目治理规则补充。
- 现象：用户要求后续开发阶段交互时详细描述“为什么这样做”和“做了什么”，并在需要人工介入时及时停下来反馈，确认清楚后再继续。
- 影响：后续所有开发阶段的过程同步和最终汇报都需要更适合归档；遇到阻塞或高风险动作时不能继续推进相关主链路。
- 根因：项目会长期分阶段演进，需要为后续人工阅读、归档、审查和接手保留完整决策上下文。
- 修复：更新 `AGENTS.md`、`task_plan.md`、`progress.md` 和本 Skill，新增交互归档标准与人工介入标准。
- 验证：运行规则关键词检索和 Skill 校验，确认新规则已写入并且 Skill 仍有效。
- 预防：后续阶段总结必须包含原因、动作、验证、归档位置；遇到需要人工确认的事项必须暂停。
- 关联文件：`AGENTS.md`、`task_plan.md`、`progress.md`、`.agents/skills/agent-platform-project-governance/SKILL.md`。

## 2026-07-15 17:47 - PowerShell 下 Bash here-doc 命令失败

- 阶段：阶段 1，环境检查。
- 现象：执行 `python - <<'PY'` 检查 Python 包时，PowerShell 报错 `Missing file specification after redirection operator`。
- 影响：Python 包检查没有执行，无法确认 FastAPI、pytest、httpx 等依赖是否已安装。
- 根因：`<<` here-doc 是 Bash 风格语法，当前 shell 是 PowerShell，不支持该写法。
- 修复：改用 PowerShell 兼容的 `python -c` 执行内联 Python 检查。
- 验证：`python -c` 成功输出 `fastapi=False`、`uvicorn=True`、`pytest=True`、`httpx=True`。
- 预防：后续在当前项目中执行内联 Python 时优先使用 `python -c`，或使用脚本文件/PowerShell 原生命令。
- 关联文件：`progress.md`、`task_plan.md`。

## 2026-07-15 18:05 - Runtime 测试缺少 FastAPI 依赖

- 阶段：阶段 1，最小 Agent Runtime。
- 现象：运行 `python -m pytest runtime/tests -q` 时在测试收集阶段失败，错误为 `ModuleNotFoundError: No module named 'fastapi'`。
- 影响：无法验证 Python Runtime 的事件顺序和短期记忆行为。
- 根因：系统 Python 环境未安装 `runtime/requirements.txt` 中声明的 FastAPI；项目此前尚未创建 Runtime 专属虚拟环境。
- 修复：创建 `runtime\.venv`，使用 `runtime\.venv\Scripts\python -m pip install -r runtime\requirements-dev.txt` 安装 Runtime 和测试依赖。
- 验证：使用虚拟环境重跑 `runtime\.venv\Scripts\python -m pytest runtime\tests -q`，输出 `2 passed`。
- 预防：阶段 1 及后续 Runtime 验证统一使用 `runtime\.venv\Scripts\python`，不依赖系统 Python 是否安装过依赖。
- 关联文件：`runtime/requirements.txt`、`runtime/requirements-dev.txt`、`progress.md`。

## 2026-07-15 18:08 - Runtime 依赖范围过宽导致测试警告

- 阶段：阶段 1，最小 Agent Runtime。
- 现象：Runtime 测试通过但输出 `StarletteDeprecationWarning`，提示当前 TestClient 依赖组合已有弃用行为。
- 影响：测试结果不是干净输出，后续依赖重新安装可能拉取不同框架组合，降低阶段验收复现性。
- 根因：`fastapi>=0.116,<1`、`uvicorn>=0.35,<1`、`pytest>=8.4,<9` 等范围过宽，安装到了阶段 1 尚未验证的较新组合。
- 修复：将依赖收窄为 `fastapi>=0.116,<0.117`、`uvicorn[standard]>=0.35,<0.36`、`pytest>=8.4,<8.5`、`httpx>=0.28,<0.29`。
- 验证：运行 `runtime\.venv\Scripts\python -m pip check` 输出 `No broken requirements found.`；Runtime 测试输出 `2 passed` 且无警告。
- 预防：阶段交付前优先锁定已验证的次版本范围，依赖升级通过独立任务处理。
- 关联文件：`runtime/requirements.txt`、`runtime/requirements-dev.txt`。

## 2026-07-15 18:12 - Java 测试 JVM 输出 CDS 警告

- 阶段：阶段 1，最小 Agent Runtime。
- 现象：运行 `mvn -f control-plane/pom.xml test` 时输出 `Java HotSpot(TM) 64-Bit Server VM warning: Sharing is only supported for boot loader classes...`。
- 影响：测试虽然通过，但验收输出包含与业务无关的 JVM 警告，降低验证信号清晰度。
- 根因：Mockito/Spring 测试时字节码增强会追加 bootstrap classpath，Java 17 的 class data sharing 会输出提示。
- 修复：在 `maven-surefire-plugin` 中配置测试 JVM 参数 `-Xshare:off`。
- 验证：重新运行 `mvn -f control-plane/pom.xml test`，测试通过且该 JVM 警告消失。
- 预防：测试 JVM 专属参数放在 Surefire 中，不影响应用运行 JVM。
- 关联文件：`control-plane/pom.xml`。

## 2026-07-15 18:16 - PowerShell 和 curl 命令导致端到端验收误报

- 阶段：阶段 1，最小 Agent Runtime。
- 现象：`Invoke-WebRequest` 访问运行台时抛出空引用异常；随后使用 `curl.exe -d` 发送 JSON 时，服务端日志显示字段名双引号丢失并返回 400。
- 影响：端到端验收被客户端工具问题干扰，容易把命令行转义问题误判成服务端接口问题。
- 根因：当前 Windows PowerShell Web cmdlet 在该页面请求上出现客户端异常；`curl.exe` 与 PowerShell 字符串转义组合导致实际发送内容不是合法 JSON。
- 修复：页面状态用 `curl.exe -s -o NUL -w "%{http_code}"` 验证；最终端到端验收改用项目虚拟环境中的 Python `httpx` 客户端发送 JSON。
- 验证：`curl.exe` 验证页面 HTTP 200；Python `httpx` 端到端验收最终通过。
- 预防：后续复杂 JSON 验收优先使用 Python `httpx` 或脚本文件，避免在 PowerShell 命令行中手写深层 JSON 转义。
- 关联文件：`progress.md`、`docs/delivery/phase-1-showcase.md`。

## 2026-07-15 18:17 - 控制面到 Runtime 调用返回 422/500

- 阶段：阶段 1，最小 Agent Runtime。
- 现象：通过控制面创建 Agent Run 时，Runtime 返回 `422 Unprocessable Entity` 且 detail 为 body 缺失，控制面向调用方返回 500；Runtime 日志同时出现 `Unsupported upgrade request`。
- 影响：控制面无法完成到 Python Runtime 的真实端到端调用，阶段 1 主链路不成立。
- 根因：Java RestClient 到 Uvicorn 的实际请求触发了 Runtime 不支持的 HTTP 升级语义，导致 FastAPI 未收到 JSON body；同时适配器测试未覆盖真实 HTTP 边界。
- 修复：在 `HttpRuntimeClient` 中使用 `JdkClientHttpRequestFactory` 并将 JDK HTTP Client 固定为 HTTP/1.1；POST 时显式设置 `Content-Type: application/json`；新增 `HttpRuntimeClientTest` 校验 JSON 请求体和 Content-Type。
- 验证：直接 Python `httpx` 调 Runtime 成功；修复后端到端验收通过，审计事件包含 `run.started`、`model.requested`、`run.output.delta`、`model.completed`、`run.completed`。
- 预防：跨语言 HTTP 适配器必须有边界测试，并在本机端到端验收中覆盖真实 Runtime，而不仅 mock 接口。
- 关联文件：`control-plane/src/main/java/com/agentplatform/control/agent/HttpRuntimeClient.java`、`control-plane/src/test/java/com/agentplatform/control/agent/HttpRuntimeClientTest.java`。

## 2026-07-15 18:21 - HttpRuntimeClient 多构造器导致 Spring 装配失败

- 阶段：阶段 1，最小 Agent Runtime。
- 现象：为便于测试新增 `HttpRuntimeClient(RestClient restClient)` 后，启动控制面失败，错误为 `No default constructor found`。
- 影响：自动化控制器测试仍能通过，但真实应用无法启动。
- 根因：类中存在生产构造器和测试构造器，Spring 无法自动判断应该使用哪个构造器；既有控制器测试 mock 了 RuntimeClient，未覆盖真实 Bean 装配。
- 修复：给生产构造器添加 `@Autowired`；新增 `ControlPlaneApplicationSmokeTest`，保留真实 `HttpRuntimeClient` Bean 启动 Spring 上下文。
- 验证：先运行新增烟测复现失败；修复后 `ControlPlaneApplicationSmokeTest` 通过，完整 `mvn -f control-plane/pom.xml test` 输出 `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`。
- 预防：当为组件增加测试构造器或工厂入口时，必须补充真实 Spring 上下文启动测试，避免 mock 掩盖生产装配问题。
- 关联文件：`control-plane/src/main/java/com/agentplatform/control/agent/HttpRuntimeClient.java`、`control-plane/src/test/java/com/agentplatform/control/ControlPlaneApplicationSmokeTest.java`。

## 2026-07-16 11:04 - PowerShell 机械替换脚本破坏 Java 源码

- 阶段：阶段 1 后结构治理，控制面包结构重排。
- 现象：按类型拆分 `control-plane` 包结构时，机械替换脚本把 `AgentSnapshot.java`、`AgentRunCreateRequest.java` 中的 `package`、`import`、`String`、`return` 等文本替换成异常字符；同时部分 import 行出现字面量 `\r\n`。
- 影响：Java 源码无法编译，若不及时修复会阻断控制面测试和后续阶段开发。
- 根因：PowerShell replacement 数组写法错误，嵌套数组被展开后，`-replace` 实际按字符级规则执行；随后 `Set-Content -Encoding UTF8` 又在 Windows PowerShell 下写入 BOM，导致 javac 报 `\ufeff` 非法字符。
- 修复：停止使用该替换脚本；用 `apply_patch` 重建 `AgentSnapshot.java` 和 `AgentRunCreateRequest.java`；将字面量 `\r\n` 还原为真实换行；用 .NET `UTF8Encoding(false)` 将 Java 文件重写为 UTF-8 无 BOM。
- 验证：运行损坏字符扫描无命中；运行 `mvn -f control-plane/pom.xml test`，结果为 `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`。
- 预防：后续批量 Java 包名迁移优先使用 IDE/Java 专用重构工具或 `apply_patch` 分批修改；如果必须脚本化，先在单个临时样本上验证数组结构和编码，不使用 Windows PowerShell `Set-Content -Encoding UTF8` 写 Java 源码。
- 关联文件：`control-plane/src/main/java/com/agentplatform/control/agent/dto/AgentSnapshot.java`、`control-plane/src/main/java/com/agentplatform/control/agent/dto/AgentRunCreateRequest.java`、`progress.md`。

## 2026-07-16 11:27 - 代码备注统一中文规则缺失

- 阶段：阶段 1 后结构治理，Python Runtime 与控制面备注治理。
- 现象：用户明确要求“所有代码的备注，统统用中文”，但项目硬约束中只写了“注释尽量完整”，没有明确 JavaDoc、Python docstring、行注释等都必须使用中文；现有 Python 测试和 Java 控制面中仍有英文备注。
- 影响：后续人工阅读和 Agent 接手时，可能继续写入英文叙述性备注，造成项目注释风格不一致。
- 根因：早期规则强调注释完整性，没有把注释语言作为独立硬约束固化。
- 修复：更新 `AGENTS.md`、`task_plan.md` 和项目专属 Skill，明确所有代码备注统一使用中文；将现有 Java/Python 英文备注改为中文，并补齐关键 DTO 的中文说明。
- 验证：限定目录扫描 Python `#` 注释、Java `/**`、`//`、`*` 备注，确认现有备注为中文叙述；旧英文备注短语扫描无命中；`runtime\.venv\Scripts\python -m pytest runtime\tests -q` 输出 `2 passed`；`mvn -f control-plane\pom.xml clean test` 强制重编译后输出 `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0` 和 `BUILD SUCCESS`。
- 预防：后续新增或修改代码时，先检查备注语言；只保留必要的英文技术名词，解释性句子必须使用中文。
- 关联文件：`AGENTS.md`、`task_plan.md`、`.agents/skills/agent-platform-project-governance/SKILL.md`、`runtime/tests/test_runtime.py`、`control-plane/src/main/java/com/agentplatform/control/agent`。

## 2026-07-16 11:27 - 备注扫描命令误扫整个工作区

- 阶段：阶段 1 后结构治理，验证脚本执行。
- 现象：一次 `rg` 备注扫描原本要限定在 `D:\Workspace\agent-platform` 的 Python 目录，但实际输出了 `D:\Workspace` 下其他项目的大量文件。
- 影响：扫描结果不能作为当前项目验证依据，并产生大量无关输出，干扰问题判断。
- 根因：命令路径与正则参数在 PowerShell 中没有按预期限制扫描范围。
- 修复：废弃该次扫描结果，改为将工作目录固定到 `D:\Workspace\agent-platform`，并使用相对路径分别扫描 `runtime/app`、`runtime/tests`、`control-plane/src/main/java` 和 `control-plane/src/test/java`。
- 验证：重新执行限定目录扫描，输出只包含当前项目相对路径。
- 预防：后续扫描优先设置 `workdir` 为项目根目录，并使用相对路径；扫描结果若出现项目外路径，立即作废并重跑。
- 关联文件：`progress.md`、`.agents/skills/agent-platform-project-governance/references/problem-log.md`。

## 2026-07-16 12:43 - PowerShell Select-Object 取行参数写法错误

- 阶段：阶段 1 后启动命令核对。
- 现象：为摘取 `docs/delivery/phase-1-showcase.md` 的启动方式片段时，执行 `Select-Object -Index 20..50` 报错，提示无法将字符串 `20..50` 转换为 `System.Int32`。
- 影响：首次取证命令失败，但未修改业务文件，也未影响 Java 控制面和 Python Runtime 启动命令结论。
- 根因：PowerShell 将未加括号的 `20..50` 作为参数绑定字符串处理，没有先求值为整数数组。
- 修复：改为先读取文件到 `$lines`，再使用 `$lines[23..49]` 取片段。
- 验证：重新输出 `phase-1-showcase.md` 的“启动方式”片段，确认包含 Runtime 依赖安装、Python Runtime 启动、Java 控制面启动和运行台地址。
- 预防：后续使用 `Select-Object -Index` 传范围时使用括号，例如 `-Index (20..50)`；简单片段可优先用数组下标取值。
- 关联文件：`docs/delivery/phase-1-showcase.md`、`.agents/skills/agent-platform-project-governance/references/problem-log.md`。

## 2026-07-16 12:58 - Windows PowerShell 解析 UTF-8 无 BOM 脚本失败

- 阶段：阶段 1 后 Windows 启停脚本补充。
- 现象：新增 `.ps1` 启停脚本后，用 Windows PowerShell 执行语法解析和 `-CheckOnly` 时，中文字符串被误读为乱码并触发 `Unexpected token`、字符串缺少结束符等解析错误。
- 影响：脚本内容本身的逻辑尚未执行，但 Windows PowerShell 5.1 无法稳定解析 UTF-8 无 BOM 且包含中文的 `.ps1` 文件，会阻断本机启停脚本使用。
- 根因：Windows PowerShell 5.1 在缺少 BOM 时容易按系统 ANSI 代码页读取脚本；中文 UTF-8 字节被误解码后可能破坏字符串边界。
- 修复：保留中文注释和提示文本，将 `scripts/windows/*.ps1` 机械转换为 UTF-8 with BOM；同时收窄 Java 停止脚本的端口兜底匹配，避免 PID 文件场景因 Maven 命令行不含项目根目录名而跳过。
- 验证：5 个 `.ps1` 文件 PowerShell AST 解析均输出 `OK`；使用临时端口执行两个启动脚本的 `-CheckOnly` 均输出预期命令；两个停止脚本的 `-DryRun` 能完成，且 Java 停止脚本未误停 8080 上命令行特征不匹配的进程。
- 预防：后续新增包含中文注释或中文提示的 Windows PowerShell 脚本时，统一保存为 UTF-8 with BOM；验证时必须覆盖 AST 解析和无副作用运行参数。
- 关联文件：`scripts/windows/_process-utils.ps1`、`scripts/windows/start-python-runtime.ps1`、`scripts/windows/stop-python-runtime.ps1`、`scripts/windows/start-java-control-plane.ps1`、`scripts/windows/stop-java-control-plane.ps1`。

## 2026-07-16 15:56 - 停止脚本 DryRun 提示误导

- 阶段：阶段 1 后 Windows 启停脚本补充。
- 现象：执行 `stop-python-runtime.ps1 -DryRun` 时，脚本先输出将停止的进程，但结尾仍提示 `Python Runtime 已停止。`。
- 影响：DryRun 实际没有停止进程，但提示语容易让使用者误以为进程已经停止，影响本机运维判断。
- 根因：公共停止函数只区分是否找到了匹配进程，没有在最终提示中区分真实停止和 DryRun 检查。
- 修复：在 `_process-utils.ps1` 中为 DryRun 分支输出 `dry-run 检查完成，未实际停止进程`，真实停止时才输出 `已停止`。
- 验证：重新执行 `stop-python-runtime.ps1 -DryRun`，输出 `Python Runtime dry-run 检查完成，未实际停止进程。`；重新执行 `stop-java-control-plane.ps1 -DryRun`，对命令行特征不匹配的 8080 进程只告警并跳过。
- 预防：后续所有带 DryRun/CheckOnly 的脚本，验证输出必须明确说明是否产生副作用，不能只验证退出码。
- 关联文件：`scripts/windows/_process-utils.ps1`、`scripts/windows/stop-python-runtime.ps1`、`scripts/windows/stop-java-control-plane.ps1`、`progress.md`。

## 2026-07-16 16:12 - Spring Boot argfile 导致 Java 停止脚本误判目标进程

- 阶段：阶段 1 后 Windows 启停脚本补充。
- 现象：执行 `stop-java-control-plane.ps1` 时，8002 端口上存在 `java.exe`，命令行为 `-cp @C:\Users\EDY\AppData\Local\Temp\spring-boot-*.argfile com.agentplatform.control.ControlPlaneApplication`，脚本提示命令行特征不匹配并跳过。
- 影响：当前项目 Java 控制面已经在目标端口监听，但停止脚本无法识别并停止，用户需要手工处理进程。
- 根因：Spring Boot 在 Windows 上将较长 classpath 写入 `@argfile`，外层 Java 命令行没有直接出现 `control-plane`；原匹配函数只检查外层命令行，没有读取 argfile 内容。
- 修复：新增 `Get-AgentProcessMatchText`，在匹配进程时读取命令行中的 `.argfile` 内容并与外层命令行合并判断；保留端口和项目特征双重约束，避免仅按端口误停无关服务。
- 验证：5 个 `.ps1` 文件 PowerShell AST 解析均输出 `OK`；`stop-java-control-plane.ps1 -Port 8002 -DryRun` 输出 `[DRY-RUN] 将停止进程：PID=28980 Name=java` 和 `未实际停止进程`；UTF-8 BOM 检查全部通过。
- 预防：后续识别 Java/Spring Boot 进程时，不能只依赖外层命令行；遇到 `@*.argfile` 必须展开读取后再做项目特征判断。
- 关联文件：`scripts/windows/_process-utils.ps1`、`scripts/windows/stop-java-control-plane.ps1`、`progress.md`。

## 2026-07-16 16:24 - Java 启动脚本应用参数泄漏到 Maven 层

- 阶段：阶段 1 后 Windows 启停脚本补充。
- 现象：执行 Java 控制面启动脚本时出现 `Unable to parse command line options: Unrecognized option: --agent.runtime.base-url=http://127.0.0.1:8001`；随后尝试逗号分隔参数时，Spring Boot 将 `server.port` 绑定为 `18082,--agent.runtime.base-url=...` 并启动失败。
- 影响：Java 控制面无法通过脚本稳定启动，用户需要手工调整命令。
- 根因：`Start-Process -ArgumentList`、Maven CLI、Spring Boot Maven Plugin 和 Spring Boot 应用参数之间存在多层解析边界；带空格的 `spring-boot.run.arguments` 会让第二个 `--` 参数漏到 Maven 层，逗号形式在当前插件/应用链路中不会拆成两个命令行参数。
- 修复：启动脚本不再使用 `spring-boot.run.arguments` 传应用参数；改为在启动 Maven 前设置 `SERVER_PORT` 与 `AGENT_RUNTIME_BASE_URL` 环境变量，利用 Spring Boot relaxed binding 注入 `server.port` 和 `agent.runtime.base-url`。启动后恢复脚本进程原环境变量，避免污染调用方。
- 验证：5 个 `.ps1` 文件 AST 解析通过；`start-java-control-plane.ps1 -CheckOnly -Port 18083` 显示 Maven 命令不再携带 `--` 应用参数；临时端口 `18084` 真实启动成功，`curl.exe http://127.0.0.1:18084/index.html` 返回 HTTP 200；`stop-java-control-plane.ps1 -Port 18084` 停止后端口释放。
- 预防：Windows PowerShell 启动 Maven/Spring Boot 时，优先用环境变量传递 Spring 配置；避免把多个应用 `--` 参数塞进单个 Maven 属性，除非已做真实启动验证。
- 关联文件：`scripts/windows/start-java-control-plane.ps1`、`scripts/windows/_process-utils.ps1`、`progress.md`。

## 2026-07-16 15:59 - SSE 事件流中文显示乱码

- 阶段：阶段 1，最小 Agent Runtime 闭环修复。
- 现象：用户直接打开 `http://127.0.0.1:8002/api/v1/agent-runs/{runId}/events` 时，`run.output.delta` 中的中文内容显示为乱码。
- 影响：阶段 1 的运行过程事件流虽然能返回数据，但浏览器直接查看 SSE 地址时中文不可读，影响成果验收和调试。
- 根因：Python Runtime 生成的事件内容是正确的，但 Java 控制面通过 `SseEmitter` 转发时没有在真实响应写出边界强制 UTF-8；`Content-Type` 只有 `text/event-stream`，浏览器或调试工具可能按错误编码展示中文。
- 修复：在 `AgentRunController.streamEvents` 中设置响应编码为 UTF-8，设置 `Content-Type` 为 `text/event-stream;charset=UTF-8`，并让每条 SSE data 以 JSON UTF-8 写出；新增 `eventStreamUsesUtf8ForChinesePayload` 回归测试。
- 验证：新增回归测试先因 `Content-Type` 缺少 `charset=UTF-8` 失败；修复后该测试通过。完整验证结果：`runtime\.venv\Scripts\python -m pytest runtime\tests -q` 输出 `2 passed`；`mvn -f control-plane\pom.xml clean test` 输出 `Tests run: 6, Failures: 0, Errors: 0, Skipped: 0` 和 `BUILD SUCCESS`；临时新版控制面 `8003` 真实 HTTP 验证显示 `Content-Type=text/event-stream;charset=UTF-8`，中文 payload 的 `replacement_count=0`。
- 预防：后续新增 SSE、流式文本、文件下载或跨语言文本转发接口时，测试必须覆盖中文 payload 和响应 charset；浏览器可直接访问的文本流接口必须显式声明 UTF-8。
- 关联文件：`control-plane/src/main/java/com/agentplatform/control/agent/controller/AgentRunController.java`、`control-plane/src/test/java/com/agentplatform/control/agent/controller/AgentRunControllerTest.java`、`progress.md`。

## 2026-07-16 15:59 - 本机验证命令参数和进程清理踩坑

- 阶段：阶段 1，SSE 乱码真实 HTTP 验证。
- 现象：第一次执行 Maven 定向测试时，PowerShell 将未加引号的 `#` 后内容当作注释，导致 Maven 报 `Unknown lifecycle phase`；第一次启动临时控制面时，Spring Boot 参数中的空格被拆开，Maven 误把 `--agent.runtime.base-url` 当作自身选项；停止临时进程时又误用 PowerShell 只读变量 `$PID`。
- 影响：这些问题没有修改业务源码，但延长了验证时间，并产生了无效启动和孤立探测进程。
- 根因：Windows PowerShell 对 `#`、空格参数、内置变量名有特殊语义；直接复用 Bash 风格或未加引号的命令容易误执行。
- 修复：定向测试命令改为给 `-Dtest=类名#方法名` 加引号；临时控制面改用环境变量 `SERVER_PORT=8003` 和 `AGENT_RUNTIME_BASE_URL=...` 启动；端口进程清理使用 `$listenPid`，避免覆盖 `$PID`。
- 验证：临时新版控制面在 `8003` 启动成功并完成真实 HTTP 验证；验证后通过 `netstat` 确认 `8003` 无 `LISTENING` 进程，仅剩短暂 `TIME_WAIT`。
- 预防：PowerShell 下 Maven 定向测试包含 `#` 时必须加引号；Spring Boot 多运行参数优先使用环境变量或配置文件；脚本变量避免使用 `$PID` 等内置变量名。
- 关联文件：`progress.md`、`.agents/skills/agent-platform-project-governance/references/problem-log.md`。

## 2026-07-16 17:20 - GitHub 远程仓库授权规则调整

- 阶段：项目治理与远程归档。
- 现象：项目早期规则写明“暂不执行 Git 提交、推送”，用户随后创建 GitHub 仓库并明确要求提交当前项目、推送到远程。
- 影响：如果不更新规则，后续 Agent 会看到旧规则并误判当前 Git 操作违反项目约束；如果直接删除约束，又会让后续未授权 Git 写操作失去保护。
- 根因：项目从本机单机开发阶段进入远程归档阶段，Git 策略需要从“完全不执行写操作”调整为“默认禁止，用户明确授权后可执行指定写操作”。
- 修复：更新 `AGENTS.md`、`README.md`、`task_plan.md` 和项目专属 Skill，记录本次用户授权范围：初始化仓库、提交当前项目、推送到 `https://github.com/atlantis68/agent-platform.git`。
- 验证：后续执行 `git status`、敏感信息扫描、自动化测试、提交和推送结果作为本次归档验证依据。
- 预防：未来 Git 写操作仍需用户明确授权；一次授权只覆盖用户明确指定的动作，不自动扩展到合并、打 tag 或发布链路。
- 关联文件：`AGENTS.md`、`README.md`、`task_plan.md`、`progress.md`、`.agents/skills/agent-platform-project-governance/SKILL.md`。

## 2026-07-16 17:18 - PowerShell AST 检查命令未初始化 ref 变量

- 阶段：提交前验证。
- 现象：执行 Windows 脚本 AST 检查时，PowerShell 报错 `[ref] cannot be applied to a variable that does not exist`。
- 影响：首次 AST 检查未能证明脚本语法状态，但没有修改业务文件，也没有影响 Runtime 和控制面测试。
- 根因：检查命令在 `ForEach-Object` 中直接使用 `[ref]$errs`，但 `$errs` 变量尚未初始化；PowerShell 不能对不存在的变量应用 `[ref]`。
- 修复：在每次解析前显式初始化 `$tokens = $null` 和 `$parseErrors = $null`，再传入 `[ref]$tokens` 与 `[ref]$parseErrors`。
- 验证：重新执行修正后的 AST 检查命令，输出 `PowerShell AST OK`。
- 预防：后续使用 PowerShell `[ref]` 参数时，先初始化变量；验证命令失败时先区分“检查命令问题”和“被检查脚本问题”。
- 关联文件：`scripts/windows/*.ps1`、`progress.md`。

## 2026-07-16 17:25 - GitHub 推送缺少本机认证凭据

- 阶段：远程归档。
- 现象：本地首次提交已创建，但执行 `git push -u origin main` 时长时间无输出；中断挂起进程后，用非交互模式重试返回 `fatal: could not read Username for 'https://github.com': terminal prompts disabled`。
- 影响：本地提交已完成，但无法推送到 `https://github.com/atlantis68/agent-platform.git`；远程仓库仍等待首次推送。
- 根因：本机没有可用于 HTTPS 推送的 GitHub 凭据；`gh` 未安装，SSH 探测 `git@github.com` 返回 `Permission denied (publickey)`，因此没有可替代的已认证通道。
- 修复：当前需要用户在本机完成 GitHub HTTPS 凭据授权，或配置具备仓库写权限的 SSH key 后再重试 `git push -u origin main`。
- 验证：`git status --short --branch` 显示本地在 `main` 分支且无未提交业务文件；非交互 push 明确返回凭据缺失；SSH 探测明确返回 publickey 权限不足。
- 预防：后续首次推送 GitHub 前，先确认 `git push` 所需认证方式；不要在未确认凭据时长时间等待交互式进程。
- 关联文件：`progress.md`、`.agents/skills/agent-platform-project-governance/references/problem-log.md`。
