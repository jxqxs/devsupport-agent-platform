# 01｜DevSupport Agent Platform：12 周总路线图

> 用途：只看整体节奏。  
> 每天“怎么做”看 `02_DevSupport_daily_lab_manual.md`；  
> 原理与面试看 `03_DevSupport_knowledge_and_interview_tutorial.md`；  
> 最终系统怎么拼看 `04_DevSupport_project_engineering_manual.md`。
>
> 默认环境：Cursor Remote SSH → Ubuntu 24.04 Server。

# 0. 最终目标

最终完成：

> **DevSupport Agent Platform：企业研发支持与故障排查 Agent 平台**

最终流程：

```text
用户问题
  ↓
Intent Router
  ↓
并行检索：官方 Docs / GitHub Issues / 社区案例
  ↓
Merge + Rerank
  ↓
ReAct Tool Loop
  ↓
Answer Verifier / Confidence Check
  ↓
回答 或 HITL / 创建工单
```

技术栈：

```text
Java 21
Spring Boot 4.1.x
PostgreSQL + pgvector
Redis
Python 3.12
FastAPI
LangGraph
MCP
Go 1.27
Docker Compose
Nginx
```

---

# Week 1｜Java 基础与业务模型

## 为什么学

后面的 Spring Boot、Redis、线程池、Agent Backend 都依赖这些 Java 基础。

## 每天

- Day 1：建仓库；Object / equals / hashCode；Ticket / KnowledgeSource
- Day 2：泛型；Result<T> / ApiResponse<T>
- Day 3：Collection；ArrayList / LinkedList / HashSet
- Day 4：HashMap；put/get；可变 key 实验
- Day 5：Exception；BusinessException / ErrorCode
- Day 6：Lambda / Stream；用 Stream 处理 Ticket；周复盘

## 主项目映射

```text
Ticket Entity
KnowledgeSource Entity
统一返回结构
统一异常结构
业务数据聚合
```

---

# Week 2｜JUC + JVM + MySQL

## 为什么学

为后面的并发请求、异步任务、Agent 并行检索、线上 JVM 排错、Ticket 查询优化打基础。

## 每天

- Day 1：Race Condition、synchronized、AtomicInteger、CAS
- Day 2：ThreadPoolExecutor、Queue、Reject、100 Ticket 并发实验
- Day 3：CompletableFuture、ThreadLocal、ReentrantLock
- Day 4：JVM 内存、GC/STW、G1/ZGC、Arthas、OOM/StackOverflow
- Day 5：B+Tree、聚簇/二级索引、回表、覆盖索引
- Day 6：联合索引、EXPLAIN、10 万 Ticket 数据实验

## 主项目映射

```text
Agent 请求并发
并行检索
Request ID
Ticket 查询优化
线上性能排查
```

---

# Week 3｜Spring Boot 主后端

- Day 1：Spring Initializr、Health API
- Day 2：Controller / Service / Repository、Ticket CRUD
- Day 3：PostgreSQL、Entity / DTO / VO、表设计
- Day 4：Validation、GlobalExceptionHandler、统一响应
- Day 5：KnowledgeSource API、文档源 metadata
- Day 6：OpenAPI / curl 测试、周复盘

## 主项目映射

```text
用户
工单
知识源
会话
Agent 配置
```

---

# Week 4｜Redis + JWT + RBAC

- Day 1：Redis 数据结构、Spring Redis
- Day 2：穿透/击穿/雪崩实验
- Day 3：JWT、Access / Refresh Token
- Day 4：BCrypt、注册/登录
- Day 5：USER / SUPPORT / ADMIN
- Day 6：Redis Session、Logout / Token revoke

## 主项目映射

```text
登录
权限
Token
Knowledge Cache
```

---

# Week 5｜Linux + Docker + Nginx

- Day 1：进程、端口、日志、资源排查
- Day 2：Image / Container / Volume
- Day 3：Docker Network；localhost 故障实验
- Day 4：Dockerfile；Spring Boot 容器化
- Day 5：Compose；Postgres / Redis / Backend
- Day 6：Nginx；502 排错；服务器部署

---

# Week 6｜Go LLM Gateway

- Day 1：Go 语法、struct / interface
- Day 2：goroutine / channel / WaitGroup
- Day 3：context timeout / cancel
- Day 4：Gin：/health /models /chat
- Day 5：Provider Interface、Mock / OpenAI-compatible
- Day 6：retry、rate limit、streaming、request_id

---

# Week 7｜Python FastAPI + Tool Calling + 真实数据

- Day 1：venv、FastAPI、Pydantic
- Day 2：LLM API、roles、streaming
- Day 3：Structured Output、Tool Schema
- Day 4：手写 Tool Calling Loop
- Day 5：下载 Docker Docs / Kubernetes Docs，记录 License / Commit
- Day 6：GitHub Issues API → JSONL

真实数据入口：

- Docker Docs：https://github.com/docker/docs
- Kubernetes Docs：https://github.com/kubernetes/website
- GitHub Issues API：https://docs.github.com/en/rest/issues/issues

---

# Week 8｜RAG + pgvector

- Day 1：清洗、统一 schema
- Day 2：Heading-aware Chunk
- Day 3：Embedding、pgvector 入库
- Day 4：Vector Search、Metadata Filter
- Day 5：Hybrid Search、Issue Search、来源优先级
- Day 6：Hit@K、MRR、RAG Eval

---

# Week 9｜LangGraph

- Day 1：State / Node / Edge
- Day 2：Router / Conditional Edge
- Day 3：Parallel Retrieval
- Day 4：ReAct Tool Loop
- Day 5：Verifier / Confidence Check
- Day 6：Checkpoint / HITL / Failure Recovery

最终 Agent 架构：

> **Hybrid Router-ReAct Workflow**

---

# Week 10｜MCP + Spring AI

- Day 1：MCP Host / Client / Server
- Day 2：document-mcp
- Day 3：issue-mcp
- Day 4：ticket-mcp / ops-mcp
- Day 5：Spring AI ChatClient / Tool / Memory
- Day 6：Spring AI RAG / MCP，对比 Python LangGraph

资料：

- MCP：https://blog.modelcontextprotocol.io/posts/2026-07-28/
- Spring AI：https://docs.spring.io/spring-ai/reference/

---

# Week 11｜Evaluation + Trace + Reliability

- Day 1：request_id / Trace Schema
- Day 2：Retrieval Eval
- Day 3：Tool Eval
- Day 4：Answer / Citation Eval
- Day 5：timeout / retry / fallback / circuit breaker
- Day 6：故障注入；P95 / TTFT / Failure Rate

---

# Week 12｜收口与求职

- Day 1：README、架构图
- Day 2：5 个真实 Demo Case
- Day 3：性能 / Eval 结果
- Day 4：项目追问题库
- Day 5：GitHub 清理、一键启动
- Day 6：Mock Interview、简历改写

---

# 四份文档怎么用

```text
今天学什么？        → 01 总路线图
具体怎么一步步做？  → 02 逐日实验任务书
为什么这样做？      → 03 知识点教程+面试
最后整体怎么拼？    → 04 主项目工程手册
```
