# DevSupport Agent Platform：核心实战开发手册


# 服务器优先开发说明

> **本套任务默认全部在 Ubuntu 24.04 云服务器完成。**
>
> 本机 Windows 只负责：
>
> ```text
> Cursor / VS Code
>       ↓ Remote SSH
> Ubuntu Server
>       ↓
> 所有代码、数据库、Docker、Java、Python、Go 都在服务器执行
> ```
>
> 因此后续命令默认都是 Linux Bash，不再给 PowerShell 版本。
>
> 推荐工作目录：
>
> ```bash
> mkdir -p ~/projects
> cd ~/projects
> ```
>
> 主项目路径：
>
> ```text
> ~/projects/devsupport-agent-platform
> ```
>
> 服务器资源有限时的原则：
>
> - 不在服务器跑本地大模型
> - LLM 使用外部 API
> - 向量库优先 PostgreSQL + pgvector
> - 不上 Milvus / Elasticsearch 第一版
> - Docker Compose 中控制服务数量
> - 避免同时启动无关实验容器
>
> 建议所有长期运行服务都通过 Docker Compose 或 systemd 管理，不依赖临时终端窗口。
>
> ---

> 这份文档解决的是：**项目到底怎么从 0 做出来。**
>
> 不是只讲架构，而是包含：
> - 数据下载
> - 数据许可记录
> - 数据清洗格式
> - 数据库表
> - Java API
> - Python RAG
> - Go Gateway
> - MCP
> - LangGraph
> - Evaluation
> - 部署与排错

---

# 0. 推荐开发方式：Cursor Remote SSH

你本机只打开 Cursor，然后连接服务器：

```text
Cursor
→ Remote SSH
→ agent-cloud
→ /home/ubuntu/projects/devsupport-agent-platform
```

所有终端命令都在 Cursor 的服务器终端执行。

建议：

```bash
cd ~/projects/devsupport-agent-platform
pwd
git status
```

确认当前永远在服务器项目目录。

如果 Cursor 新开终端后不确定：

```bash
hostname
whoami
pwd
```

不要在 Windows 本地和服务器同时维护两套源码，否则很容易出现：
- 本地改了服务器没改
- `.env` 不一致
- Docker 跑在另一台机器
- 数据库地址混乱

---

# 1. 产品定义

## 1.1 目标用户

目标用户不是“随便聊天的普通用户”，而是：

```text
研发工程师
测试工程师
运维工程师
内部技术支持
新员工
```

## 1.2 解决的问题

典型问题：

```text
Docker Compose 服务互联失败
Kubernetes Pod 异常
容器 Volume / Network 问题
Spring Boot 启动失败
Redis / PostgreSQL 基础故障
重复出现的历史 Issue
内部工单处理
```

## 1.3 核心业务闭环

```text
用户问题
 ↓
问题分类
 ↓
官方文档 RAG
 ↓
历史 Issue 搜索
 ↓
必要时调用 Tool
 ↓
生成诊断
 ↓
用户确认
 ↓
创建 / 更新 Support Ticket
 ↓
保存 Trace
 ↓
进入评测统计
```

---

# 2. 数据层：真实数据怎么准备

# 2.1 Docker Docs

官方仓库：

https://github.com/docker/docs

License：

https://github.com/docker/docs/blob/main/LICENSE

下载：

```bash
mkdir -p data/raw

git clone --depth 1 \
  https://github.com/docker/docs.git \
  data/raw/docker-docs
```

记录版本：

```bash
cd data/raw/docker-docs
git rev-parse HEAD
```

输出 SHA 保存：

```text
docs/data-license/docker-docs.md
```

建议：

```markdown
# Docker Docs Source

Source: https://github.com/docker/docs
License: Apache-2.0
Commit: <sha>
Downloaded: 2026-xx-xx

Used paths:
- content/manuals/
- content/guides/
- content/reference/
```

## 为什么必须记 commit？

官方文档会更新。

RAG 如果不记录版本，将来：
- 答案变了
- Eval 结果变了
- 无法复现实验

所以 metadata 必须带：

```text
source_version
```

---

# 2.2 Kubernetes Docs

仓库：

https://github.com/kubernetes/website

License：

https://github.com/kubernetes/website/blob/main/LICENSE

下载：

```bash
git clone --depth 1 \
  https://github.com/kubernetes/website.git \
  data/raw/kubernetes-docs
```

记录：

```bash
cd data/raw/kubernetes-docs
git rev-parse HEAD
```

优先读取：

```text
content/en/docs/concepts/
content/en/docs/tasks/
content/en/docs/tutorials/
content/en/docs/reference/kubectl/
```

---

# 2.3 GitHub Issues

官方 API：

https://docs.github.com/en/rest/issues/issues

先做无需登录的 public repo 拉取。

Python：

```python
# scripts/collect_issues/github_issues.py

import json
import time
from pathlib import Path

import requests

REPOS = [
    "docker/docs",
    "kubernetes/website",
]

OUT = Path("data/raw/github-issues")
OUT.mkdir(parents=True, exist_ok=True)


def fetch_repo(owner_repo: str, max_pages: int = 5):
    owner, repo = owner_repo.split("/")

    rows = []

    for page in range(1, max_pages + 1):
        url = (
            f"https://api.github.com/repos/"
            f"{owner}/{repo}/issues"
        )

        resp = requests.get(
            url,
            params={
                "state": "all",
                "per_page": 100,
                "page": page,
                "sort": "updated",
                "direction": "desc",
            },
            headers={
                "Accept": "application/vnd.github+json"
            },
            timeout=30,
        )

        resp.raise_for_status()

        items = resp.json()

        if not items:
            break

        for x in items:
            # GitHub 的 Issue endpoint 也会返回 PR
            if "pull_request" in x:
                continue

            rows.append({
                "repo": owner_repo,
                "number": x["number"],
                "title": x["title"],
                "body": x.get("body") or "",
                "state": x["state"],
                "labels": [
                    label["name"]
                    for label in x.get("labels", [])
                ],
                "comments": x.get("comments", 0),
                "html_url": x["html_url"],
                "created_at": x["created_at"],
                "updated_at": x["updated_at"],
                "closed_at": x.get("closed_at"),
            })

        time.sleep(1)

    return rows


for repo in REPOS:
    rows = fetch_repo(repo)

    path = OUT / (
        repo.replace("/", "__") + ".jsonl"
    )

    with path.open("w", encoding="utf-8") as f:
        for row in rows:
            f.write(
                json.dumps(
                    row,
                    ensure_ascii=False
                ) + "\n"
            )

    print(repo, len(rows), path)
```

安装：

```bash
pip install requests
```

运行：

```bash
python scripts/collect_issues/github_issues.py
```

## API 限流

未认证请求额度有限。

后期需要更多数据时：
- 配 GitHub Token
- 只存到本地 `.env`
- 不提交仓库

---

# 2.4 GH Archive：可选大规模数据

官网：

https://www.gharchive.org/

示例：

```bash
wget \
  https://data.gharchive.org/2026-09-01-0.json.gz
```

适合：
- 研究公开 GitHub 活动
- 批量 IssueEvent

但项目第一阶段不要用它取代 REST API。

原因：
- 数据大
- 需要自己过滤
- 工程复杂度高但面试收益未必高

---

# 2.5 Stack Overflow

完整数据：

https://archive.org/details/stackexchange

Data Explorer：

https://data.stackexchange.com/

License：

https://stackoverflow.com/help/licensing

推荐做法：

### 第一阶段

Data Explorer / 手工选择真实问题：

```text
100~300 条
```

标签：

```text
docker
docker-compose
kubernetes
spring-boot
redis
postgresql
```

保存：

```json
{
  "question_id": 123,
  "title": "...",
  "body": "...",
  "tags": ["docker"],
  "accepted_answer": "...",
  "source_url": "...",
  "license": "CC BY-SA ...",
  "created_at": "..."
}
```

### 第二阶段

如果你真需要更多，再处理 XML Dump。

不要为了“数据量大”先把 90GB 全拉下来。

---

# 3. 统一知识数据 Schema

所有数据先归一化。

```python
from pydantic import BaseModel
from typing import Literal


class KnowledgeDocument(BaseModel):
    document_id: str

    source_type: Literal[
        "official_doc",
        "github_issue",
        "stackoverflow"
    ]

    product: str
    title: str
    content: str
    source_url: str

    license: str | None = None
    source_version: str | None = None

    tags: list[str] = []
```

为什么统一？

因为后面可以：

```text
统一 chunk
统一 embedding
统一 metadata filter
统一 citation
```

---

# 4. 文档清洗

Markdown 清洗原则：

保留：
- title
- heading
- paragraph
- code block
- list

删除 / 降低：
- 导航模板
- Hugo shortcode 噪声
- 页面 footer
- 无意义 HTML

不要直接：

```text
strip 掉所有代码块
```

因为 DevSupport 中：

```text
命令
配置
错误日志
```

往往非常重要。

---

# 5. Chunk 设计

第一版：

```text
按 Markdown Heading 分段
↓
过长段落再切
↓
目标 500~800 tokens
↓
overlap 80~120 tokens
```

metadata：

```json
{
  "source_type": "official_doc",
  "product": "docker",
  "document_id": "...",
  "heading": "Networking in Compose",
  "source_url": "...",
  "license": "Apache-2.0",
  "source_version": "sha..."
}
```

不要只保存：

```text
content + embedding
```

否则无法引用来源。

---

# 6. PostgreSQL + pgvector

Docker Compose：

```yaml
postgres:
  image: pgvector/pgvector:pg17
  environment:
    POSTGRES_USER: devsupport
    POSTGRES_PASSWORD: devsupport
    POSTGRES_DB: devsupport
  ports:
    - "5432:5432"
  volumes:
    - postgres_data:/var/lib/postgresql/data
```

启：

```bash
docker compose up -d postgres
```

SQL：

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

核心表：

```sql
CREATE TABLE knowledge_documents (
    id BIGSERIAL PRIMARY KEY,
    document_id VARCHAR(255) NOT NULL UNIQUE,
    source_type VARCHAR(32) NOT NULL,
    product VARCHAR(64) NOT NULL,
    title TEXT NOT NULL,
    source_url TEXT NOT NULL,
    license VARCHAR(64),
    source_version VARCHAR(128),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE document_chunks (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL
        REFERENCES knowledge_documents(id),
    chunk_index INT NOT NULL,
    heading TEXT,
    content TEXT NOT NULL,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    embedding VECTOR(1536)
);

CREATE INDEX idx_document_chunks_document
ON document_chunks(document_id);

CREATE INDEX idx_documents_source_product
ON knowledge_documents(source_type, product);
```

`VECTOR(1536)` 只是示例。

必须根据你实际 embedding model 维度改。

---

# 7. Java 后端：业务职责

Java 不做“RAG 算法”。

Java 负责稳定业务：

```text
User
Auth
RBAC
Ticket
Ticket Comment
Knowledge Source
Document Metadata
Agent Config
Conversation
Audit
```

---

# 8. Java 数据库表

Users：

```sql
CREATE TABLE app_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    email VARCHAR(128) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'USER',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

Ticket：

```sql
CREATE TABLE support_tickets (
    id BIGSERIAL PRIMARY KEY,
    creator_id BIGINT NOT NULL
        REFERENCES app_users(id),
    assignee_id BIGINT
        REFERENCES app_users(id),

    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,

    category VARCHAR(64),
    priority VARCHAR(16) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',

    resolution TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

Comments：

```sql
CREATE TABLE ticket_comments (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL
        REFERENCES support_tickets(id),
    author_id BIGINT NOT NULL
        REFERENCES app_users(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

# 9. Java API

Auth：

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
```

Ticket：

```text
POST   /api/tickets
GET    /api/tickets/{id}
GET    /api/tickets
PATCH  /api/tickets/{id}/status
POST   /api/tickets/{id}/comments
```

Knowledge：

```text
GET  /api/knowledge-sources
POST /api/knowledge-sources
GET  /api/documents/{id}
```

Agent：

```text
POST /api/agent/query
GET  /api/agent/runs/{requestId}
```

---

# 10. Agent API

Python：

```text
POST /internal/agent/run
```

请求：

```json
{
  "request_id": "uuid",
  "user_id": 100,
  "message": "docker compose 里的 backend 连不上 postgres",
  "context": {
    "ticket_id": null
  }
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
  "suggested_actions": [
    {
      "type": "CREATE_TICKET",
      "requires_approval": true
    }
  ]
}
```

---

# 11. Retrieval 策略

不要所有数据混一起 Top-K。

建议：

```text
Query
 ↓
Intent
 ↓
official_docs retrieval: top 8
historical_issue retrieval: top 5
community_qa retrieval: top 3
 ↓
rerank
 ↓
evidence pack
```

官方文档优先级最高。

Prompt 可写规则：

```text
1. 官方文档为主要事实来源。
2. GitHub Issue 只能作为历史案例。
3. Stack Overflow 只能作为社区参考。
4. 来源冲突时明确指出，不把社区回答说成官方结论。
```

这本身就是很好的工程亮点。

---

# 12. Hybrid Search

为什么需要？

错误码：

```text
CrashLoopBackOff
ECONNREFUSED
ORA-...
```

语义向量未必最稳。

可以做：

```text
Vector Search
+
Keyword / FTS
+
Rerank
```

PostgreSQL 可以先用：
- pgvector
- PostgreSQL full-text search

不必一开始上 Elasticsearch。

---

# 13. Go LLM Gateway

Provider：

```go
type Provider interface {
    Chat(
        ctx context.Context,
        req ChatRequest,
    ) (*ChatResponse, error)
}
```

服务：

```text
POST /chat
POST /chat/stream
GET /models
GET /health
```

Gateway 负责：

```text
Provider Adapter
Timeout
Retry
Rate Limit
Request ID
Metrics
Streaming
Fallback
```

Agent 服务只看统一协议。

---

# 14. MCP

## document-mcp

Tools：

```text
search_document(query, product, top_k)
get_document(document_id)
```

## issue-mcp

```text
search_issues(query, repo, top_k)
get_issue(repo, number)
```

## ops-mcp

第一版只做安全无害的模拟：

```text
get_service_status(service)
get_recent_logs(service, lines)
```

不要第一版就：

```text
shell(command)
execute_sql(sql)
```

---

# 15. LangGraph

State：

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

Graph：

```text
START
  ↓
classify
  ↓
retrieve_official
  ↓
retrieve_cases
  ↓
compose
  ↓
verify_sources
  ↓
confidence?
 ├─ high → answer
 └─ low  → suggest_ticket
  ↓
END
```

---

# 16. Evaluation

Eval Dataset：

```json
{
  "id": "docker_001",
  "question": "Compose 中容器如何通过服务名互联？",
  "category": "docker_network",
  "expected_source_type": "official_doc",
  "expected_product": "docker",
  "expected_facts": [
    "same Compose network",
    "service name"
  ]
}
```

至少 100 条。

指标：

```text
Retrieval Hit@5
MRR
Tool Selection Accuracy
Answer Fact Pass Rate
Citation Source Accuracy
Workflow Success Rate
TTFT
P95 Latency
```

---

# 17. Trace

每次请求：

```json
{
  "request_id": "...",
  "user_id": 1,
  "intent": "docker_network",

  "retrieval": {
    "official_doc_ids": ["..."],
    "issue_ids": ["..."]
  },

  "tool_calls": [],

  "model": "...",

  "input_tokens": 1400,
  "output_tokens": 260,

  "ttft_ms": 300,
  "total_latency_ms": 2100,

  "error": null
}
```

---

# 18. 数据许可与引用

README 必须写：

```text
Docker documentation:
Source: https://github.com/docker/docs
License: Apache-2.0

Kubernetes documentation:
Source: https://github.com/kubernetes/website
License: CC BY 4.0

Stack Overflow:
Public contributions are licensed under applicable CC BY-SA versions.
See: https://stackoverflow.com/help/licensing
```

GitHub Issues：
- 保留原 issue URL
- 不假装内容是你创建的
- 如果公开发布处理后的数据集，应额外检查对应 repo / 平台条款

最稳妥方式：

> 项目仓库提供“采集脚本”，而不是直接把大量第三方内容重新打包提交 GitHub。

---

# 19. Docker Compose 最终架构

```text
nginx
 ↓
backend-java
 ├── postgres/pgvector
 └── redis
 ↓
agent-python
 ↓
llm-gateway-go

mcp-document
mcp-issue
```

4GB 云服务器注意：

```text
不要 Milvus
不要 Elasticsearch 第一版
不要本地大模型
```

使用：
- PostgreSQL + pgvector
- Redis
- 外部模型 API

---

# 20. 项目最重要的 8 个演示场景

1. Docker Compose localhost 错误
2. Docker Volume 数据持久化问题
3. Kubernetes CrashLoopBackOff
4. Kubernetes readiness probe
5. 查历史 GitHub Issue
6. 官方文档与历史 Issue 同时召回
7. 低置信度自动建议创建工单
8. LLM 超时后 fallback / 错误 Trace

这 8 个比做一个“你好，我是 Agent”有价值得多。

---

# 21. 服务器日常开发与排错命令

## 21.1 查看资源

```bash
free -h
df -h
htop
```

## 21.2 查看监听端口

```bash
ss -lntp
```

例如：

```bash
ss -lntp | grep 8080
ss -lntp | grep 5432
ss -lntp | grep 6379
```

## 21.3 查看 Docker

```bash
docker ps
docker compose ps
docker stats
```

## 21.4 查看日志

```bash
docker compose logs -f backend
docker compose logs -f postgres
docker compose logs -f redis
```

只看最近 100 行：

```bash
docker compose logs --tail=100 backend
```

## 21.5 清理无用 Docker 资源

先看：

```bash
docker system df
```

再谨慎：

```bash
docker image prune
docker container prune
```

不要随便：

```bash
docker system prune -a
```

尤其数据库 volume 未确认前不要乱删。

## 21.6 推荐 tmux

安装：

```bash
sudo apt install -y tmux
```

创建：

```bash
tmux new -s devsupport
```

离开但保持任务：

```text
Ctrl+B
D
```

回来：

```bash
tmux attach -t devsupport
```

但长期服务仍推荐 Docker Compose，不要依赖 tmux 维持生产式服务。
