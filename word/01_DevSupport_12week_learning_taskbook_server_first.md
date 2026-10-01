# DevSupport Agent Platform：12 周实习学习任务书（2026 版）


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

> 项目定位：**企业研发支持与故障排查 Agent 平台**
>
> 目标岗位：
> - Java 后端实习
> - AI 应用 / Agent 开发实习
> - LLM 应用工程实习
> - 部分 Go 后端实习
>
> 最终不是做一个“能聊天的 Agent Demo”，而是做一个有真实数据、真实业务闭环、真实后端工程能力的垂直项目：
>
> ```text
> 研发人员提出问题
>        ↓
> DevSupport Agent
>        ↓
> Intent / Router
>  ┌─────┼────────┐
>  ↓     ↓        ↓
> 官方文档  历史 Issue  运维 / 业务 Tool
> RAG     Search     MCP
>  └─────┼────────┘
>        ↓
> 故障分析 + 排查步骤
>        ↓
> 必要时执行受控 Tool
>        ↓
> 生成 / 更新工单
>        ↓
> Trace + Evaluation
> ```
>
> 核心主线：Java 后端 > 数据库/Redis/工程化 > Python Agent/RAG > Go Gateway。
> 建议投入：每天 4~6 小时，每周 6 天。

---

# 0. 三份文档怎么配合

你现在会有三份文档：

1. **本文件：12 周学习任务书**
   - 每周学什么
   - 看哪些视频 / 哪些 P
   - 环境怎么配
   - 当天代码做到哪里
   - 每周验收

2. **《核心实战开发手册》**
   - DevSupport Agent Platform 从 0 到 1 的完整工程设计
   - 数据怎么下
   - 数据怎么清洗
   - SQL 怎么建
   - API 怎么设计
   - Java / Go / Python / MCP / RAG 代码结构怎么落地

3. **《每周知识点自学与面试手册》**
   - 不看视频也能复习
   - 每个 P 节点讲知识点
   - 高频知识题
   - 场景题
   - 面试回答思路

---

# 1. 项目最终目录

```text
devsupport-agent-platform/
├── backend-java/                # Spring Boot 主业务后端
│   └── devsupport-backend/
├── agent-python/                # FastAPI + LangGraph + RAG
├── llm-gateway-go/              # Go 模型网关
├── mcp-servers/
│   ├── document-mcp/
│   ├── issue-mcp/
│   └── ops-mcp/
├── data/
│   ├── raw/
│   │   ├── docker-docs/
│   │   ├── kubernetes-docs/
│   │   ├── github-issues/
│   │   └── stackoverflow/
│   ├── processed/
│   └── eval/
├── scripts/
│   ├── ingest_docs/
│   ├── collect_issues/
│   └── build_eval/
├── deploy/
│   ├── docker-compose.yml
│   └── nginx/
├── docs/
│   ├── architecture/
│   ├── api/
│   ├── experiments/
│   ├── interview/
│   └── data-license/
└── README.md
```

---

# 2. 真实数据来源：从第一天就确定

## 2.1 Docker 官方文档

用途：
- RAG 主知识库之一
- Docker / Compose / Network / Volume / Build / Engine 故障支持

官方仓库：

https://github.com/docker/docs

许可证：

https://github.com/docker/docs/blob/main/LICENSE

下载：

```bash
git clone --depth 1 https://github.com/docker/docs.git data/raw/docker-docs
```

建议第一阶段只读取：

```text
data/raw/docker-docs/content/manuals/
data/raw/docker-docs/content/guides/
data/raw/docker-docs/content/reference/
```

不要一上来把仓库所有模板、前端文件都入库。

---

## 2.2 Kubernetes 官方文档

用途：
- Pod / Deployment / Service / ConfigMap / Secret / Probe / Volume / Scheduling / Troubleshooting

官方仓库：

https://github.com/kubernetes/website

许可证：

https://github.com/kubernetes/website/blob/main/LICENSE

下载：

```bash
git clone --depth 1 https://github.com/kubernetes/website.git data/raw/kubernetes-docs
```

重点内容通常在：

```text
content/en/docs/
```

第一阶段可优先导入：

```text
concepts/
tasks/
tutorials/
reference/kubectl/
```

---

## 2.3 GitHub Issues：真实历史工单

GitHub Issues REST API：

https://docs.github.com/en/rest/issues/issues

推荐先用 API，不要一开始下载全 GH Archive。

示例：

```bash
curl -L \
  -H "Accept: application/vnd.github+json" \
  "https://api.github.com/repos/docker/docs/issues?state=all&per_page=100"
```

注意：GitHub Issues API 可能同时返回 Pull Request；JSON 里带 `pull_request` 字段的记录要过滤。

建议采集：
- docker/docs
- kubernetes/website
- 之后再增加 1~2 个 Java / Spring 相关公开仓库

目标量：

```text
Week 7~8：
500~1500 条 Issue

最终：
1000~3000 条即可
```

大规模研究可选 GH Archive：

https://www.gharchive.org/

按小时下载示例：

```bash
wget https://data.gharchive.org/2026-09-01-0.json.gz
```

但**本项目第一版不需要全量 GH Archive**。

---

## 2.4 Stack Overflow：真实技术问答

用途：
- 评测集
- 社区历史案例库
- Hybrid Search 的关键词场景

Stack Exchange Data Dump：

https://archive.org/details/stackexchange

发布历史说明：

https://meta.stackexchange.com/questions/224873/all-stack-exchange-data-dump-releases

许可说明：

https://stackoverflow.com/help/licensing

完整 Dump 非常大，不建议第一次直接下载几十 GB。

学习阶段优先：
1. 用 Stack Exchange Data Explorer 抽样 CSV；
2. 或只下载 Stack Overflow Posts 包；
3. 或先人工整理 100~300 条与 docker/kubernetes/spring-boot/redis/postgresql 相关 QA 作为 eval。

Data Explorer：

https://data.stackexchange.com/

---

# 3. 服务器环境基线

## 3.1 第一次登录服务器

本机只需要：

```bash
ssh agent-cloud
```

进入后确认：

```bash
whoami
pwd
uname -a
lsb_release -a
```

推荐统一在：

```text
/home/ubuntu/projects
```

开发。

创建：

```bash
mkdir -p ~/projects
cd ~/projects
```

---

## 3.2 安装通用开发工具

```bash
sudo apt update

sudo apt install -y \
  git \
  curl \
  wget \
  unzip \
  zip \
  vim \
  htop \
  tree \
  jq \
  build-essential \
  ca-certificates \
  gnupg \
  lsof \
  net-tools \
  software-properties-common
```

检查：

```bash
git --version
curl --version
gcc --version
```

---

## 3.3 安装 JDK 21

```bash
sudo apt install -y openjdk-21-jdk
```

检查：

```bash
java -version
javac -version
```

确认 Java 21。

查看路径：

```bash
readlink -f $(which java)
```

---

## 3.4 安装 Maven

```bash
sudo apt install -y maven
```

检查：

```bash
mvn -version
```

确认 Maven 使用的是 JDK 21。

---

## 3.5 Python 环境

Ubuntu 24.04 通常已有 Python 3.12：

```bash
python3 --version
```

安装 venv：

```bash
sudo apt install -y python3-venv python3-pip
```

后续所有 Python 项目都用虚拟环境：

```bash
python3 -m venv .venv
source .venv/bin/activate
```

不要直接全局 `pip install`。

---

## 3.6 安装 Go 1.27

先看：

```bash
go version
```

若版本不符，再安装官方版本。

示例：

```bash
cd /tmp

wget https://go.dev/dl/go1.27.0.linux-amd64.tar.gz

sudo rm -rf /usr/local/go
sudo tar -C /usr/local -xzf go1.27.0.linux-amd64.tar.gz
```

添加环境变量：

```bash
echo 'export PATH=$PATH:/usr/local/go/bin' >> ~/.bashrc
source ~/.bashrc
```

检查：

```bash
go version
```

---

## 3.7 安装 Docker Engine + Compose

推荐服务器直接装 Docker Engine，不依赖 Docker Desktop。

如果系统已经有 Docker：

```bash
docker --version
docker compose version
```

如果没有，可按 Docker 官方 Ubuntu 文档安装：

https://docs.docker.com/engine/install/ubuntu/

安装后：

```bash
sudo usermod -aG docker $USER
```

退出 SSH 后重新登录：

```bash
exit
ssh agent-cloud
```

测试：

```bash
docker run --rm hello-world
```

---

## 3.8 Git 配置

```bash
git config --global user.name "你的名字"
git config --global user.email "你的邮箱"
git config --global init.defaultBranch main
```

检查：

```bash
git config --global --list
```

---

## 3.9 服务器资源检查

以后每次服务异常，先会看：

```bash
free -h
df -h
htop
docker ps
docker stats
```

你的实验全部在服务器上进行，因此必须养成“开发 + 运维一起看”的习惯。

---

# Week 1：Java 基础 + 建仓库 + 项目数据模型

## 本周目标

你不是单纯刷 Java。

你要完成：

```text
DevSupport 项目总仓库
+
纯 Java User / Ticket / KnowledgeSource 数据模型
+
Result<T>
+
异常体系
+
集合实验
+
HashMap 实验
```

## 视频

Java 课程：

https://www.bilibili.com/video/BV1izGR6EELo/

### Object

- P199  
  https://www.bilibili.com/video/BV1izGR6EELo/?p=199
- P200  
  https://www.bilibili.com/video/BV1izGR6EELo/?p=200
- P201  
  https://www.bilibili.com/video/BV1izGR6EELo/?p=201
- P202  
  https://www.bilibili.com/video/BV1izGR6EELo/?p=202

### Exception

- P257  
  https://www.bilibili.com/video/BV1izGR6EELo/?p=257
- P259  
  https://www.bilibili.com/video/BV1izGR6EELo/?p=259
- P261  
  https://www.bilibili.com/video/BV1izGR6EELo/?p=261
- P262  
  https://www.bilibili.com/video/BV1izGR6EELo/?p=262
- P264~266  
  https://www.bilibili.com/video/BV1izGR6EELo/?p=264  
  https://www.bilibili.com/video/BV1izGR6EELo/?p=265  
  https://www.bilibili.com/video/BV1izGR6EELo/?p=266

### 泛型

P324~331：

https://www.bilibili.com/video/BV1izGR6EELo/?p=324

依次把 `p=` 改为 325~331。

### Collection / ArrayList

- P336 / 337 / 340 / 342 / 344 / 347
- P349 / 351 / 352 / 353 / 355 / 358

例如：

https://www.bilibili.com/video/BV1izGR6EELo/?p=349

### HashMap

P389~401：

https://www.bilibili.com/video/BV1izGR6EELo/?p=389

重点：390、391、393~401。

### Lambda / Stream

- P570 / 571 / 576 / 577
- P586 / 587 / 589 / 590 / 593 / 597 / 599 / 600

---

## Day 1：创建总仓库

服务器：

```bash
cd ~/projects

mkdir -p devsupport-agent-platform
cd devsupport-agent-platform

git init

mkdir -p   backend-java   agent-python   llm-gateway-go   mcp-servers   data/raw   data/processed   data/eval   scripts   deploy   docs/architecture   docs/experiments   docs/interview   docs/data-license
```

创建：

```text
data/raw
data/processed
data/eval
docs/architecture
docs/experiments
docs/interview
docs/data-license
```

第一次 commit：

```powershell
git add .
git commit -m "chore: initialize DevSupport Agent workspace"
```

---

## Day 2：创建纯 Java Lab

```bash
cd backend-java

mvn archetype:generate `
  -DgroupId=com.example.devsupport `
  -DartifactId=java-basics-lab `
  -DarchetypeArtifactId=maven-archetype-quickstart `
  -DinteractiveMode=false
```

创建：

```text
model/User.java
model/Ticket.java
model/KnowledgeSource.java
common/Result.java
common/ErrorCode.java
exception/BusinessException.java
```

`Ticket` 最少字段：

```text
id
title
description
status
priority
source
createdAt
updatedAt
```

`KnowledgeSource`：

```text
id
sourceType        OFFICIAL_DOC / GITHUB_ISSUE / STACKOVERFLOW
product           docker / kubernetes / spring...
title
sourceUrl
license
```

---

## Day 3：集合与 HashMap 实验

必须亲手跑：

1. ArrayList vs LinkedList 随机访问
2. HashSet + equals/hashCode
3. 可变对象做 HashMap key 导致查找异常
4. Stream 对 Ticket 按状态过滤 / 分组

示例：

```java
Map<String, List<Ticket>> grouped =
        tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getStatus));
```

---

## Day 4：异常与业务错误码

定义：

```text
TICKET_NOT_FOUND
KNOWLEDGE_SOURCE_NOT_FOUND
USERNAME_EXISTS
INVALID_PARAMETER
```

保证你以后 Spring Boot 直接复用这种思路。

---

## Day 5：设计业务流程图

画到：

```text
docs/architecture/week1-business-flow.md
```

内容：

```text
User asks:
"docker compose backend cannot connect postgres"
        ↓
Intent = docker/network
        ↓
Search official docs
        ↓
Search historical issues
        ↓
Generate diagnosis
        ↓
Create support ticket if unresolved
```

---

## Day 6：脱离教程重写

要求：
- 不看旧代码重新写 Result<T>
- 不看旧代码写 Ticket
- 口述 HashMap put
- 做两道 LeetCode Medium

### Week 1 验收

你必须能解释：

```text
为什么这个项目需要 Ticket
为什么需要 KnowledgeSource metadata
为什么官方文档和 Issue 不应该混成一种 source
```

---

# Week 2：JVM + JUC + MySQL

## 视频

JVM：

https://www.bilibili.com/video/BV1bY9KBYEAF/

- P1 内存模型
- P2 GC
- P3 STW
- P4 Arthas
- P7 CMS / G1 / ZGC

MySQL / Redis：

https://www.bilibili.com/video/BV1CTLJ6PE2o/

MySQL：

- P1 大表索引
- P2 B+Tree
- P3 聚簇 / 覆盖 / ICP
- P4 联合索引
- P5 EXPLAIN
- P6 最左前缀

---

## Day 1~2：JUC 实验

写：

```text
CounterDemo
AtomicIntegerDemo
ReentrantLockDemo
ThreadLocalDemo
ThreadPoolDemo
CompletableFutureDemo
```

场景模拟：

```text
并发处理 100 个 Support Ticket
```

每个 Ticket 模拟调用外部知识检索：

```java
Thread.sleep(100);
```

然后比较：
- 串行
- 固定线程池
- CompletableFuture

---

## Day 3：MySQL Docker

```bash
docker run -d \
  --name mysql8 \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -p 3306:3306 \
  mysql:8
```

建：

```text
support_tickets
users
ticket_comments
```

`tickets` 建议字段：

```sql
CREATE TABLE support_tickets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(64),
    status VARCHAR(32) NOT NULL,
    priority VARCHAR(16) NOT NULL,
    assignee_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);
```

---

## Day 4~5：10 万工单索引实验

生成 10 万虚拟工单用于数据库性能实验。

注意：
- 这是**数据库压测测试数据**
- 不冒充真实工单
- 真实知识数据仍来自官方 docs / GitHub Issues

实验：

```text
(status, created_at)
(category, status)
assignee_id
```

跑 EXPLAIN。

---

## Day 6：JVM / MySQL 面试复盘

输出：

```text
docs/experiments/mysql-index.md
docs/interview/week02.md
```

---

# Week 3：Spring Boot 4.1 + DevSupport Backend v0.1

## 官方

Spring Boot：

https://docs.spring.io/spring-boot/reference/

当前学习基线：4.1.x。

## 视频

https://www.bilibili.com/video/BV1iykjB1Ewc/

重点：

```text
P1
P2
P10
P11
P14
P15
P16
P17
P18
```

---

## Day 1：Spring Initializr

创建：

```text
backend-java/devsupport-backend
```

设置：

```text
Maven
Java
Spring Boot 4.1.x
Java 21
Group: com.example
Artifact: devsupport-backend
```

依赖：

```text
Spring Web
Validation
Actuator
PostgreSQL Driver
Spring Security（Week4再重点用）
```

先写：

```text
GET /health
```

---

## Day 2：模块分层

```text
controller
service
repository
entity
dto
vo
exception
config
common
```

实现 Ticket CRUD：

```text
POST   /api/tickets
GET    /api/tickets/{id}
GET    /api/tickets
PUT    /api/tickets/{id}
PATCH  /api/tickets/{id}/status
```

---

## Day 3：PostgreSQL

主项目改用 PostgreSQL。

表：

```text
users
tickets
ticket_comments
knowledge_sources
documents
agents
conversations
messages
```

---

## Day 4：Validation + GlobalExceptionHandler

必须测试：

```text
空 title
非法 priority
不存在 ticket
```

---

## Day 5：Knowledge Source API

```text
POST /api/knowledge-sources
GET  /api/knowledge-sources
```

示例：

```json
{
  "sourceType": "OFFICIAL_DOC",
  "product": "docker",
  "name": "Docker Official Docs",
  "sourceUrl": "https://github.com/docker/docs",
  "license": "Apache-2.0"
}
```

---

## Day 6：Swagger / OpenAPI + 周测

做到从 Swagger：

```text
创建 Ticket
→ 添加评论
→ 修改状态
→ 创建 Knowledge Source
```

---

# Week 4：Redis + JWT + 权限 + 缓存

## 视频

Redis：

https://www.bilibili.com/video/BV1CTLJ6PE2o/

重点：

```text
P11 P12 P13 P14 P15 P16 P17 P18 P20
```

场景课：

https://www.bilibili.com/video/BV1qb6EB2EGi/

重点：

```text
P1 P2 P3 P4 P7 P9 P12
```

---

## 本周业务目标

增加：

```text
注册
登录
JWT
Refresh Token
Redis Session
RBAC
Ticket 权限
Knowledge Source 缓存
```

角色：

```text
USER
SUPPORT
ADMIN
```

权限示例：

```text
USER     创建自己的 Ticket / 查看自己的
SUPPORT  查看待处理 Ticket / 回复
ADMIN    管理知识源 / Agent 配置
```

---

# Week 5：Docker / Linux / Nginx / 云部署

## 视频

https://www.bilibili.com/video/BV1HP4118797/

重点：

```text
P3 P4 P5 P7 P8 P9 P10 P11 P12 P14
```

## 本周目标

Compose：

```text
nginx
backend-java
postgres
redis
```

云服务器部署：

```bash
docker compose up -d --build
```

你要会排查：

```text
502
数据库连接失败
Redis 连接失败
端口占用
磁盘满
日志爆量
```

---

# Week 6：Go 1.27 + LLM Gateway

官方：

https://go.dev/doc/go1.27  
https://go.dev/doc/tutorial/  
https://go.dev/tour/concurrency/1

## 最终接口

```text
GET  /health
GET  /models
POST /chat
POST /chat/stream
```

## 本周必须实现

```text
Provider Interface
MockProvider
OpenAI-Compatible Provider
context timeout
request_id
rate limit
retry
streaming
```

业务意义：

> Agent Python 不直接依赖具体模型厂商，而统一调用 Gateway。

---

# Week 7：Python FastAPI + Tool Calling + 开始真实数据采集

## Python 服务目录

```text
agent-python/app/
├── main.py
├── api/
├── tools/
├── services/
├── schemas/
└── core/
```

## Tool

至少：

```text
get_ticket(ticket_id)
list_user_tickets(user_id)
search_official_docs(query)
search_historical_issues(query)
```

---

## 本周真实数据任务 A：Docker Docs

```bash
git clone --depth 1 \
  https://github.com/docker/docs.git \
  data/raw/docker-docs
```

写：

```text
docs/data-license/docker-docs.md
```

记录：
- 来源
- URL
- 下载日期
- commit SHA
- Apache-2.0
- 你实际使用哪些目录

---

## 本周真实数据任务 B：Kubernetes Docs

```bash
git clone --depth 1 \
  https://github.com/kubernetes/website.git \
  data/raw/kubernetes-docs
```

记录：
- 来源
- commit SHA
- CC BY 4.0
- 归属说明

---

## 本周真实数据任务 C：GitHub Issues

写：

```text
scripts/collect_issues/github_issues.py
```

目标：
- 拉 docker/docs issues
- 拉 kubernetes/website issues
- 过滤 PR
- 保存 JSONL

每条至少：

```json
{
  "repo": "docker/docs",
  "number": 123,
  "title": "...",
  "body": "...",
  "state": "closed",
  "labels": ["..."],
  "comments_url": "...",
  "html_url": "...",
  "created_at": "...",
  "updated_at": "..."
}
```

---

# Week 8：RAG + pgvector + 真实知识库

## 视频

LangChain 1.3：

https://www.bilibili.com/video/BV1HgK86zEzr/

```text
P1 P2 P3 P4 P5 P6
P7 P8 可提前看 MCP
```

官方：

https://docs.langchain.com/oss/python/learn

---

## Day 1：文档预处理

统一转换为：

```json
{
  "document_id": "...",
  "source_type": "official_doc",
  "product": "docker",
  "title": "...",
  "content": "...",
  "source_url": "...",
  "license": "Apache-2.0",
  "version": "git-commit-sha"
}
```

---

## Day 2：Chunk

Baseline：

```text
chunk_size=600
overlap=100
```

但必须保留 metadata。

---

## Day 3：Embedding + pgvector

表：

```text
documents
document_chunks
```

每个 chunk 必须能反查：
- document
- source
- URL
- license
- version

---

## Day 4：检索

实现：

```text
semantic_search
metadata_filter
```

例如：

```text
product = docker
source_type = official_doc
```

---

## Day 5：历史 Issue 搜索

Issue 不与官方文档混为完全同一种可信度。

回答策略：

```text
官方 docs = primary evidence
GitHub Issues = historical case
Stack Overflow = community reference
```

---

## Day 6：RAG Evaluation

自己建立 100 条 eval：

```text
Docker 40
Kubernetes 40
Spring/Linux 20
```

每条：

```json
{
  "question": "...",
  "expected_source_type": "official_doc",
  "expected_product": "docker",
  "expected_keywords": ["network", "service name"]
}
```

---

# Week 9：LangGraph 工作流

## 视频

https://www.bilibili.com/video/BV1mrdwBtEEH/

重点：

```text
P19 P20 P21 P22 P23 P24 P25
P26 P27 可选
```

## 工作流

```text
START
 ↓
classify_problem
 ├─────────────┬─────────────┐
 ↓             ↓             ↓
official_rag  issue_search  general_chat
 └─────────────┼─────────────┘
               ↓
        compose_diagnosis
               ↓
       confidence_check
         ↓          ↓
       enough     not enough
         ↓          ↓
      answer    create_ticket
         ↓
        END
```

加入：

```text
retry_count
error
confidence
sources
```

---

# Week 10：MCP + Spring AI

## MCP

规范：

https://blog.modelcontextprotocol.io/posts/2026-07-28/

Transport：

https://github.com/modelcontextprotocol/modelcontextprotocol/blob/main/docs/specification/2026-07-28/basic/transports/index.mdx

做：

```text
document-mcp
issue-mcp
ops-mcp
```

Tool 示例：

```text
search_document
get_document
search_issues
get_issue
get_ticket
create_ticket
```

禁止暴露：

```text
execute_sql(sql)
```

---

## Spring AI

官方：

https://docs.spring.io/spring-ai/reference/

API：

https://docs.spring.io/spring-ai/reference/api/

视频：

https://www.bilibili.com/video/BV12ju16hEYM/

重点：

```text
P1 P2 P3 P4 P5 P6 P7 P8 P9 P10
```

短课：

https://www.bilibili.com/video/BV1DAoZBRERx/

重点：

```text
P9 Structured Output
P10 Memory
P11 Tool
P12 Embedding
P13 Vector DB
P14 RAG
P15 MCP
```

---

# Week 11：Evaluation / Trace / Reliability

## 目标

不是“我试了一下效果挺好”。

必须有：

```text
Tool Selection Accuracy
Retrieval Hit Rate
Answer Pass Rate
Source Citation Accuracy
Avg / P95 Latency
TTFT
Failure Rate
Token Cost
```

Trace：

```text
request_id
conversation_id
intent
retrieved_doc_ids
issue_ids
tool_calls
model
tokens
latency
retry
error
```

故障注入：

```text
Postgres down
Redis down
LLM timeout
MCP server down
pgvector empty
Tool 500
```

---

# Week 12：真实数据补全 + 简历 + 演示

## Stack Overflow

建议只做小规模真实 eval / case library。

入口：

https://data.stackexchange.com/

完整数据：

https://archive.org/details/stackexchange

许可：

https://stackoverflow.com/help/licensing

建议筛：

```text
docker
docker-compose
kubernetes
spring-boot
redis
postgresql
```

---

## 最终 Demo 必须准备 5 个问题

例如：

### Case 1

```text
Docker Compose 中 Spring Boot 无法连接 postgres，
DB_HOST=127.0.0.1，应该怎么排查？
```

系统应该：
- 检索 Docker 官方文档
- 找到 Compose networking
- 指出 localhost 的容器语义
- 给修改建议
- 给来源链接

### Case 2

```text
Kubernetes Pod CrashLoopBackOff 怎么排查？
```

### Case 3

```text
Readiness Probe 一直失败会发生什么？
```

### Case 4

```text
根据历史 Issue，是否有人遇到过类似 Docker Docs 构建错误？
```

### Case 5

```text
问题仍未解决，帮我创建 Support Ticket。
```

---

# 每周统一完成标准

每周都必须产出：

```text
代码
README
实验记录
至少 1 张流程图
至少 5 个 commit
3 个真实 Bug 记录
10 道面试题复盘
```

周总结：

```text
docs/weekXX-summary.md
```

必须写：

```text
本周做了什么
遇到了什么
怎么排查
为什么这样设计
还有什么问题
如果重做会怎么改
```

---

# 最终简历项目一句话

> **DevSupport Agent Platform：**面向研发支持与故障排查的企业 Agent 平台，以 Spring Boot 实现用户、工单、权限及知识源管理，以 Docker/Kubernetes 官方文档、公开 GitHub Issues 和 Stack Overflow 技术问答构建可追溯知识库，通过 pgvector + RAG + LangGraph + MCP 实现文档检索、历史案例检索、故障诊断、受控 Tool 调用与工单处理，并使用 Go LLM Gateway 统一模型调用、超时、重试、限流和 Streaming，同时构建 Retrieval / Tool / Agent 级离线评测与链路 Trace。