# bank 项目长期约定

## 架构铁律
- **依赖方向单向：** bank-bootstrap → bank-chat → bank-nlu → bank-common，只允许上层依赖下层。
- `bank-common` 绝不反向依赖任何模块；`bank-nlu` 绝不依赖 `bank-chat`；跨层复用一律把接口下沉到 common。
- 起步阶段保持 4 模块不拆。父 pom 预留的 bank-rag / bank-knowledge / bank-dialog / bank-tool / bank-compliance 五个模块，等 chat 复杂度上来再拆。
- 桩实现（Stub）放在 chat 层而非 controller，确保跨模块调用链路被真实验证。

## 技术栈版本（写死在父 pom，升级需同步核对）
Spring Boot 3.5.15 / Spring Cloud 2025.0.3 / Spring Cloud Alibaba 2025.0.0.0 / Spring AI 1.1.8 / Spring AI Alibaba 1.1.2.0 / Java 17 / MyBatis-Plus 3.5.15 / Redisson 3.52.0 / Lucene 10.1.0

## 代码与规范约定
- **模块坐标与包根统一为 `com.dz`**：父 pom groupId = `com.dz`，三个子模块内部依赖、dependencyManagement 里的内部模块全部同名 groupId，改任一处必须四处同步（曾因写成 com.bank.ai 导致内部依赖 version 匹配不上 → 整个依赖树失效，Lombok / Lucene 集体标红）。
- **Java 包根是 `com.dz.*`**，按层分包：`com.dz.api`（R/ErrorCode/请求响应 DTO）、`com.dz.web`（过滤器等）、`com.dz.context`（RequestContext / TraceId）、`com.dz.exception`（BizException / GlobalExceptionHandler）。**不是** com.bank.ai.* —— 后续新建类、写 `scanBasePackages`、`@SpringBootApplication` 位置一律用 `com.dz` 根。
- **启动类是 `com.dz.BankApplication`**，注解为 `@SpringBootApplication(scanBasePackages = "com.dz")`。**新类必须放在 com.dz 包下面**：曾把 ChatController 放到 `com.controller`、Knife4jConfig 放到 `com.config`，两个包都在 com.dz 之外 → 扫不到 → 接口 404、配置类不生效。
- 配置文件是 `bank-bootstrap/src/main/resources/application.yaml`（.yaml 不是 .yml）；顶层节点（`mybatis-plus` / `knife4j` / `springdoc` / `management` / `logging`）**不能缩进到 `spring` 里面**，否则静默失效（logback pattern 失效 → 日志里看不到 traceId）。
- **Spring Boot 3 必须用 `jakarta.validation.*`。** 坑点：`spring-ai-starter-model-ollama` 会传递带入 `javax.validation:validation-api:1.1.0`，导致写成 `javax.validation.Valid` **能编过但运行时被 Spring 6 / Hibernate Validator 8 忽略**（校验静默失效，不报错）。
- 子模块引依赖**一律不写 version**，统一由父 pom 的 BOM / dependencyManagement 管理。
- pom 注释**禁止**使用 `<!-- ---------- xxx ---------- -->` 这种双连字符分隔线（非法 XML，Maven 直接 Fatal Error），改用 `==========`。
- 只有 bank-bootstrap 启用 spring-boot-maven-plugin 的 repackage，其余模块是库。

## 业务与文档
- **业务细节唯一权威来源：** 用户提供的技术文档 `D:\xwechat_files\wxid_etyjkgyfjdwy22_7953\msg\file\2026-09\银行文档.md`（保定银行智能客服系统 RAG 检索引引擎）。7 大领域 / 32 类意图 / 七步安检 / PII 四级 / 路由阈值等全部以此为准，**不要自行编造**。

## 工作方式偏好
- **用户倾向于自己在工程里粘贴代码**，要求我把代码直接输出在对话里，而不是写入 `D:\IdeaProjects\bank` 的文件（原话："你不用帮我写，把代码发出来就行"）。除非明确要求写文件，否则默认在对话中给出代码。
- 用户的简历在 `C:\Users\PC\Desktop\我的简历.pdf`，项目叙述需与简历口径保持一致。

## 环境备忘
- **本机已有 Maven（之前记错，已更正）：** 安装路径 `D:\envs\apache-maven-3.9.14\bin\mvn.cmd`；本地仓库 **`D:\repository`**（不是 ~/.m2/repository）；settings `D:\envs\apache-maven-3.9.14\conf\settings.xml`；JDK `D:\envs\jdk17`（JAVA_HOME=D:\envs\jdk17）；IDEA `D:\JetBrains\IntelliJ IDEA 2026.2.1`。
- **命令行跑构建的方式**（Git Bash 里 PATH 损坏，必须写全绝对路径 + Windows 风格参数）：
  `JAVA_HOME='D:\envs\jdk17' /d/envs/apache-maven-3.9.14/bin/mvn.cmd -s D:\envs\apache-maven-3.9.14\conf\settings.xml -Dmaven.repo.local=D:\repository -f D:\IdeaProjects\bank\pom.xml -B validate`
  注意：`-f` 必须用 Windows 风格路径，`/d/...` 形式会报 "file does not exist"。
- **pom 改完先验 `mvn -B validate`**（秒级，能抓 ModelValidator 级别的错），需要确认依赖能否拉通再跑 `dependency:resolve`。
- **已知坑：spring-ai-alibaba-bom（1.1.0.0 / 1.1.2.0 / 1.1.2.4 / 2.0.0-M1.1）不管 dashscope starter**，它只管 graph / agent / nacos / studio / sandbox 等模块。所以 `com.alibaba.cloud.ai:spring-ai-alibaba-starter-dashscope` **必须在父 pom dependencyManagement 里显式锁版本**（已在父 pom 第 8 节用 ${spring-ai-alibaba.version} 锁）。
- 中间件现状：**已有 Redis + MySQL，没有 Milvus**（M3 时再定方案）。
- 大模型走**阿里云百炼 DashScope**（与 pom 里的 starter 一致）。Key 严禁硬编码进仓库。
  - **属性名：`spring.ai.dashscope.api-key`**（配在 `bank-bootstrap/src/main/resources/application.yaml` 的 `spring.ai.dashscope` 下）。
  - 三级解析优先级（源码 `DashScopeConnectionUtils.resolveConnectionProperties`）：`spring.ai.dashscope.chat.api-key`（模型级） > `spring.ai.dashscope.api-key`（公共） > **环境变量 `AI_DASHSCOPE_API_KEY`**（注意前缀是 `AI_`，不是官方 SDK 的 `DASHSCOPE_API_KEY`）。
  - 默认值不用配：base-url = `https://dashscope.aliyuncs.com`，默认 chat 模型 = `qwen-plus`。
  - **实测：不配 key 应用直接启动失败**（`Assert.hasText` 在 bean 创建期抛 `IllegalArgumentException: DashScope API key must be set.`），而且最先炸的是 `dashscopeAgentApi`（Agent 自动配置），所以只想放行 chat 不行。M0/M1/M2 想不配 key 就跑，用 `spring.ai.dashscope.enabled: false` 整块关掉（已验证该属性控制 `@ConditionalOnDashScopeEnabled`，matchIfMissing=true）。

## 启动阻塞点（实测，按出现顺序）
1. **DashScope 缺 key** → 见上，用 `spring.ai.dashscope.enabled: false` 或配 key 解决。
2. **Milvus 启动即连库** → bank-chat 引入的 `spring-ai-starter-vector-store-milvus` 会在启动时建连（默认 localhost:19530），本机没有 Milvus 就报 `DEADLINE_EXCEEDED` 并启动失败。该自动配置**没有 enabled 开关**，只能显式排除：
   `spring.autoconfigure.exclude: org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusVectorStoreAutoConfiguration`
   → **M3 真正接 Milvus 时务必记得把这行删掉。**
3. 两个都解决后启动正常，Ollama starter 不阻塞启动。
