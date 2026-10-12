# 04｜DevSupport Agent Platform：主项目工程手册

> 这份文档只讲：
>
> **最终 DevSupport Agent Platform 怎么完整拼起来。**
>
> 基础实验去看：
>
> - `02_DevSupport_daily_lab_manual.md`
>
> 原理去看：
>
> - `03_DevSupport_knowledge_and_interview_tutorial.md`

# 1. 产品定义

DevSupport Agent Platform：

> 面向企业研发支持与故障排查的 Agent 平台。

主要用户：

```text
研发工程师
测试工程师
运维工程师
内部技术支持
```

典型问题：

```text
Docker Compose 网络问题
Kubernetes Pod 异常
Spring Boot 启动失败
Redis / PostgreSQL 基础故障
查历史 GitHub Issue
创建支持工单
```

---

# 2. Agent 架构

正式定义：

> **Hybrid Router-ReAct Workflow**

组合：

```text
Router
+
Sequential Workflow
+
Parallel Retrieval
+
ReAct Tool Loop
+
Evaluator / Verifier
+
HITL
```

流程：

```text
User
 ↓
Intent Router
 ↓
┌───────────┬───────────┬───────────┐
↓           ↓           ↓
Official    Issues      Tool
Docs RAG    Search      MCP
└───────────┼───────────┘
            ↓
        Parallel Merge
            ↓
          Rerank
            ↓
       ReAct Agent
            ↓
       Answer Draft
            ↓
  Source / Safety Verifier
       ↓            ↓
     Pass         Retry
       ↓
 Confidence Check
   ↓          ↓
 Answer    HITL / Ticket
```

第一版不做 Multi-Agent。

原因：

```text
单 Agent + Tools 已经够用
延迟更低
Token 更少
调试更简单
评测更容易
```

---

# 3. 项目目录

```text
devsupport-agent-platform/
├── backend-java/
│   └── devsupport-backend/
├── agent-python/
├── llm-gateway-go/
├── mcp-servers/
│   ├── document-mcp/
│   ├── issue-mcp/
│   └── ticket-mcp/
├── data/
│   ├── raw/
│   ├── processed/
│   └── eval/
├── scripts/
├── deploy/
├── docs/
└── README.md
```

---

# 4. 真实数据来源

## Docker Docs

仓库：

```text
https://github.com/docker/docs
```

下载：

```bash
git clone --depth 1   https://github.com/docker/docs.git   data/raw/docker-docs
```

记录版本：

```bash
cd data/raw/docker-docs
git rev-parse HEAD
```

必须保存：

```text
source URL
license
commit SHA
download date
```

---

## Kubernetes Docs

仓库：

```text
https://github.com/kubernetes/website
```

下载：

```bash
git clone --depth 1   https://github.com/kubernetes/website.git   data/raw/kubernetes-docs
```

---

## GitHub Issues

官方 API：

```text
https://docs.github.com/en/rest/issues/issues
```

保存字段：

```text
repo
number
title
body
state
labels
html_url
created_at
updated_at
closed_at
```

注意：

> GitHub Issues API 也可能返回 Pull Request，需要过滤 `pull_request` 字段。

---

## Stack Overflow

第一版只做：

```text
Eval
+
Community Case
```

入口：

```text
https://data.stackexchange.com/
https://archive.org/details/stackexchange
```

---

# 5. 数据可信等级

必须从 Schema 层面区分：

```text
official_doc
→ Primary Evidence

github_issue
→ Historical Case

stackoverflow
→ Community Reference
```

回答规则：

1. 官方文档是主要事实依据
2. GitHub Issue 只能作为历史案例
3. Stack Overflow 是社区参考
4. 来源冲突时明确指出，不能把 Issue 说成官方规范

---

# 6. 统一数据 Schema

```json
{
  "document_id": "docker_xxx",
  "source_type": "official_doc",
  "product": "docker",
  "title": "Networking in Compose",
  "content": "...",
  "source_url": "...",
  "license": "Apache-2.0",
  "source_version": "git-sha",
  "tags": []
}
```

作用：

```text
统一清洗
统一 Chunk
统一 Embedding
统一 Metadata Filter
统一 Citation
```

---

# 7. Chunk

流程：

```text
Markdown
↓
Heading
↓
Section
↓
过长 Section 再按 Token 切
```

Baseline：

```text
500~800 tokens
overlap 80~120
```

必须保留：

```text
document_id
heading
source_type
product
source_url
license
source_version
```

技术文档中的：

```text
命令
代码块
配置
错误码
```

不能全部清掉。

---

# 8. PostgreSQL + pgvector

启用：

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

核心表：

```sql
CREATE TABLE knowledge_documents (
    id BIGSERIAL PRIMARY KEY,
    document_id VARCHAR(255)
        NOT NULL UNIQUE,
    source_type VARCHAR(32)
        NOT NULL,
    product VARCHAR(64)
        NOT NULL,
    title TEXT NOT NULL,
    source_url TEXT NOT NULL,
    license VARCHAR(64),
    source_version VARCHAR(128)
);

CREATE TABLE document_chunks (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL
        REFERENCES knowledge_documents(id),
    chunk_index INT NOT NULL,
    heading TEXT,
    content TEXT NOT NULL,
    metadata JSONB NOT NULL
        DEFAULT '{}'::jsonb,
    embedding VECTOR(1536)
);
```

注意：

```text
1536 只是示例
```

必须和实际 Embedding Model 维度一致。

---

# 9. Java Backend

Java 是稳定业务后端。

负责：

```text
Auth
User
RBAC
Ticket
Ticket Comment
Knowledge Source
Agent Config
Conversation
Audit
```

核心 API：

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout

POST /api/tickets
GET  /api/tickets/{id}
GET  /api/tickets
PATCH /api/tickets/{id}/status
POST /api/tickets/{id}/comments

GET  /api/knowledge-sources
POST /api/knowledge-sources
```

---

# 10. Python Agent

接口：

```text
POST /internal/agent/run
```

请求：

```json
{
  "request_id": "uuid",
  "user_id": 1,
  "message": "docker compose backend 连不上 postgres",
  "ticket_id": null
}
```

响应：

```json
{
  "answer": "...",
  "intent": "docker_network",
  "sources": [
    {
      "type": "official_doc",
      "title": "...",
      "url": "..."
    }
  ],
  "suggested_actions": []
}
```

---

# 11. Retrieval 策略

不要把所有来源混成一个 Top-K。

建议：

```text
official docs  top 8
github issue   top 5
stackoverflow  top 3
```

然后：

```text
merge
↓
rerank
↓
evidence pack
```

---

# 12. Hybrid Search

技术问题包含大量：

```text
CrashLoopBackOff
ECONNREFUSED
ClassName
CLI command
Error Code
```

因此：

```text
Vector Search
+
Keyword Search
```

通常比纯向量更稳。

第一版建议：

```text
pgvector
+
PostgreSQL Full Text Search
```

先不引入 Elasticsearch。

---

# 13. Go LLM Gateway

接口：

```text
GET  /health
GET  /models
POST /chat
POST /chat/stream
```

Provider：

```go
type Provider interface {
    Chat(
        ctx context.Context,
        req ChatRequest,
    ) (*ChatResponse, error)
}
```

负责：

```text
Provider Adapter
Timeout
Retry
Rate Limit
Streaming
Fallback
Request ID
Token Stats
```

Python Agent 不直接绑定某个模型厂商。

---

# 14. MCP

## document-mcp

```text
search_document
get_document
```

## issue-mcp

```text
search_issues
get_issue
```

## ticket-mcp

```text
get_ticket
create_ticket
add_comment
```

禁止直接暴露：

```text
execute_sql(sql)
arbitrary shell
arbitrary filesystem
```

---

# 15. LangGraph State

```python
class AgentState(TypedDict):

    messages: list
    query: str

    intent: str | None

    official_docs: list
    issues: list
    community_cases: list

    answer: str | None
    confidence: float

    suggested_action: str | None

    retry_count: int
    error: str | None
```

---

# 16. Graph

```text
START
↓
classify_intent
↓
parallel_retrieval
├─ official
├─ issue
└─ community
↓
rerank
↓
react_tool_loop
↓
compose_answer
↓
verify_sources
├─ fail → retry
└─ pass
↓
confidence_check
├─ high → answer
└─ low → suggest_ticket
↓
END
```

---

# 17. Evaluator / Verifier

检查：

```text
有没有来源
是否优先官方文档
是否把 Issue 当成官方结论
是否虚构 Tool Result
是否真正回答用户问题
```

最多：

```text
Retry 1~2 次
```

防止无限循环。

---

# 18. HITL

高风险操作：

```text
restart_service
修改业务数据
创建重要工单
```

流程：

```text
Agent 提议
↓
Human Approval
↓
Tool Execute
↓
Audit
```

---

# 19. Trace

每次 Agent Run 记录：

```json
{
  "request_id": "...",
  "intent": "docker_network",
  "retrieved_docs": [],
  "issue_ids": [],
  "tool_calls": [],
  "model": "...",
  "input_tokens": 1200,
  "output_tokens": 300,
  "ttft_ms": 350,
  "total_latency_ms": 2200,
  "retry_count": 0,
  "error": null
}
```

---

# 20. Evaluation

## Retrieval

```text
Hit@5
MRR
```

## Tool

```text
Tool Selection Accuracy
Argument Accuracy
Tool Success Rate
```

## Answer

```text
Expected Fact Pass Rate
Citation Accuracy
```

## System

```text
Avg Latency
P95
TTFT
Failure Rate
```

---

# 21. Docker Compose 最终服务

```text
nginx
backend-java
agent-python
llm-gateway-go
postgres-pgvector
redis
document-mcp
issue-mcp
```

针对 4C4G 服务器，第一版：

```text
不跑本地 LLM
不上 Milvus
不上 Elasticsearch
模型使用外部 API
```

---

# 22. 最终 5 个 Demo

## Case 1

```text
Docker Compose 中
Spring Boot 无法连接 Postgres
DB_HOST=127.0.0.1
```

系统应该：

```text
Router
→ Docker Official Docs
→ Issue Search
→ 给出 localhost / service name 解释
→ Citation
```

## Case 2

```text
Kubernetes Pod CrashLoopBackOff
```

## Case 3

```text
Readiness Probe 一直失败
```

## Case 4

```text
查找历史相似 GitHub Issue
```

## Case 5

```text
问题仍未解决
→ Agent 建议创建 Support Ticket
→ 用户确认
→ Ticket Tool
```

---

# 23. 面试一句话

> 我做的是一个 Hybrid Router-ReAct 的研发支持 Agent。入口先路由问题类型，知识阶段并行检索 Docker/Kubernetes 官方文档、GitHub Issues 和社区案例，再做 rerank；核心 Agent 使用 ReAct 决定是否调用 MCP Tool，回答后经过 source verifier 和 confidence check，高风险操作通过 HITL。Java 负责工单、权限和知识源管理，Python LangGraph 负责编排，Go Gateway 统一模型调用，最后分别评测 Retrieval、Tool、Answer 和系统延迟。
