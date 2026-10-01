# DevSupport Agent Platform：每周知识点自学 + 面试手册


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

> 目标：即使当天不看视频，也可以通过本文完成主要理论学习。
>
> 每周结构：
> - 对应视频
> - 核心知识
> - 项目里的实际用途
> - 常见坑
> - 知识题
> - 场景题

---

# Week 1：Java 对象、异常、泛型、集合、HashMap、Stream

## Object / equals / hashCode

视频：

```text
P199~202
```

https://www.bilibili.com/video/BV1izGR6EELo/?p=199

## equals

`==`：

```text
引用是否相同
```

`equals`：

```text
逻辑语义是否相同
```

业务对象必须先想清楚“什么叫相同”。

例如 DevSupport 的 `KnowledgeSource`：

```text
Docker Official Docs
```

到底按：
- id
- URL
- name
- repository+commit

判断唯一？

这不是 Java 语法题，而是业务模型设计题。

## hashCode

HashMap：

```text
hash
 ↓
bucket
 ↓
equals
```

规则：

```text
equals 相等
→ hashCode 必须相等
```

否则 HashSet / HashMap 行为会违背预期。

### 项目实际问题

不要用可变 Ticket 对象当 Map key。

优先：

```text
Long ticketId
UUID requestId
String documentId
```

---

## Exception

视频：

```text
P257 P259 P261 P262 P264 P265 P266
```

业务项目中推荐：

```text
Repository 技术异常
 ↓
Service 业务语义
 ↓
BusinessException
 ↓
GlobalExceptionHandler
 ↓
HTTP Response
```

例如：

```text
TICKET_NOT_FOUND
KNOWLEDGE_SOURCE_NOT_FOUND
AGENT_RUN_FAILED
```

错误码是 API contract 的一部分。

---

## 泛型

视频：

```text
P324~331
```

DevSupport 常见：

```java
ApiResponse<T>
PageResponse<T>
Result<T>
```

为什么不用 Object？

因为：

```text
编译期类型安全
IDE 补全
减少强转
接口更清晰
```

### PECS

```text
Producer Extends
Consumer Super
```

理解即可，不需要把所有泛型边界题钻太深。

---

## ArrayList / LinkedList

ArrayList：
- 动态数组
- 随机访问 O(1)
- 中间插入/删除 O(n)
- 缓存局部性好

LinkedList：
- 双向链表
- 找第 i 个元素 O(n)
- 每节点额外引用
- 实际业务远没有面试里那么常用

项目里：
- 查询结果列表
- Ticket 列表
- Sources 列表

几乎默认 ArrayList 足够。

---

## HashMap

视频：

```text
P389~401
```

JDK8 典型：

```text
数组
 ↓
bucket
 ↓
链表
 ↓
必要时红黑树
```

put：

```text
hash
→ index
→ bucket
→ key compare
→ insert/replace
→ resize?
```

容量 2 的幂：
- `(n-1)&hash`
- 分桶高效
- resize 规律简单

load factor 0.75：
- 空间
- 冲突
之间折中。

树化阈值不是“理论定理”，而是实现权衡。

---

## Stream

DevSupport 实际例子：

```java
Map<String, Long> countByStatus =
    tickets.stream()
        .collect(Collectors.groupingBy(
            Ticket::getStatus,
            Collectors.counting()
        ));
```

不要把所有业务都写成 15 层 Stream。

可读性比炫技重要。

---

## Week 1 面试题

### 知识题

1. equals / hashCode 契约？
2. HashMap put 流程？
3. 为什么 key 最好不可变？
4. ArrayList / LinkedList？
5. 泛型擦除？
6. Checked / Runtime Exception？
7. Stream 中间操作为什么 lazy？

### 场景题

**问题：**

你用：

```java
ConcurrentHashMap<Long, Ticket>
```

存 Ticket，是不是就保证“同一用户不能创建两张重复工单”？

**答案方向：**

不是。

```text
线程安全
≠
业务唯一性
```

最终应：
- 定义业务唯一键
- DB unique constraint
- 幂等 key
- 事务

---

# Week 2：JVM / JUC / MySQL

## JVM

视频：

https://www.bilibili.com/video/BV1bY9KBYEAF/

P1 / P2 / P3 / P4 / P7。

运行时区域：

```text
线程共享：
Heap
Method Area / Metaspace

线程私有：
PC
Java Stack
Native Stack
```

### Heap

绝大多数对象。

DevSupport 服务中：
- JSON
- DTO
- Result
- retrieval results
- cached objects

都可能制造堆压力。

### Stack

方法调用栈帧。

无限递归：

```text
StackOverflowError
```

---

## GC

可达性分析：

```text
GC Roots
 ↓
reachable objects
```

STW：

```text
应用暂停
```

看延迟不要只看平均值。

例如：

```text
Avg = 100ms
P99 = 2s
```

用户仍可能明显觉得卡。

---

## JUC

### volatile

保证：
- 可见性
- 一定有序性

不保证：

```java
count++
```

原子。

### CAS

Compare-And-Set。

适合无锁原子变量。

ABA：

```text
A → B → A
```

需要版本戳时：
- AtomicStampedReference 思路

### ThreadPool

提交：

```text
核心线程
→ Queue
→ maximumPoolSize
→ reject
```

LLM / Tool 调用多数属于 IO-bound。

线程池不能无限开，否则：
- 下游也扛不住
- 连接池耗尽
- 内存增加

---

## MySQL B+Tree

为什么数据库索引不是普通 BST？

磁盘 IO 才是核心成本。

B+Tree 多叉：
- 树矮
- 范围扫描好
- 叶子有序

---

## 回表 / 覆盖索引

二级索引：

```text
secondary key
→ primary key
→ clustered index
→ full row
```

后一步叫回表。

如果查询列都在索引里：
- 可避免回表
- covering index

---

## 联合索引

DevSupport：

```sql
(category, status, created_at)
```

要根据真实 SQL 设计。

不要只背：

```text
选择性最高放最左
```

实际还要看：
- 查询组合
- range
- order by
- selectivity

---

## MVCC

目标：

```text
读写并发
```

依赖：
- undo
- read view
- transaction id

具体实现要结合 InnoDB。

---

## Week 2 场景题

**Ticket 表 500 万行：**

```sql
SELECT *
FROM support_tickets
WHERE status='OPEN'
ORDER BY created_at DESC
LIMIT 20;
```

很慢。

怎么排？

回答顺序：

```text
EXPLAIN
→ 数据分布
→ 当前索引
→ 是否联合索引
→ 是否回表
→ LIMIT / ORDER BY
→ 实测
```

不是一句：

> 给 status 加索引。

因为 OPEN 可能占 80%。

---

# Week 3：Spring Boot

官方：

https://docs.spring.io/spring-boot/reference/

视频：

https://www.bilibili.com/video/BV1iykjB1Ewc/

## IoC / DI

Spring 容器负责：
- 创建对象
- 组装依赖
- 生命周期
- AOP proxy

推荐：

```java
public TicketService(TicketRepository repo) {
    this.repo = repo;
}
```

构造器注入。

---

## Spring MVC

请求：

```text
Client
→ Filter
→ DispatcherServlet
→ HandlerMapping
→ Controller
→ Service
→ Repository
→ Jackson
→ Response
```

Controller：
- HTTP
- Validation
- DTO

Service：
- 业务逻辑
- transaction

Repository：
- 持久化

---

## DTO / VO / Entity

Entity 不应该直接暴露。

例如：

```text
password_hash
deleted
internal_note
```

绝对不应该因为 Entity 自动 JSON 序列化被返回。

---

## Transaction

高频失效：

```text
同类自调用
异常被吞
方法不是适当可代理形式
事务边界放错
```

项目案例：

```text
创建 Ticket
+
写第一条 comment
+
写 audit
```

如果必须一起成功：
- 同一事务

但调用 LLM：
- 不要放长数据库事务里等 20 秒

这是实际面试很好的 trade-off。

---

# Week 4：Redis / JWT / 缓存 / 分布式锁

## Redis 为什么快

```text
内存
高效结构
事件循环
IO multiplexing
较少锁竞争
```

不是只回答：

> 因为单线程。

---

## 缓存穿透

不存在的 Ticket ID：

```text
cache miss
→ DB miss
```

攻击时很危险。

手段：
- cache null
- validation
- Bloom（大规模时）

---

## 击穿

热门 Knowledge Source cache 失效：

```text
大量请求一起 DB
```

手段：
- mutex rebuild
- logical expiry
- singleflight

---

## JWT

JWT：

```text
header.payload.signature
```

签名 ≠ 加密。

payload 不放秘密。

Access + Refresh：

```text
Access 短
Refresh 长
```

即时撤销：
- Redis session
- blacklist
- token version

---

## Week 4 场景题

用户被管理员封禁，但 Access Token 还有 25 分钟。

要求 5 秒内生效。

怎么办？

答案：
- 完全 stateless JWT 做不到“即时撤销”
- 增加状态：
  - Redis user status/token version
  - blacklist
- 或短 token + gateway check

trade-off：
- 每请求查 Redis 增加依赖
- 可做本地短缓存

---

# Week 5：Docker / Linux / Nginx

> 你所有开发都在服务器上，所以这一周不是“额外学习 Linux”，而是把前几周一直在使用的服务器操作系统化总结。

Docker：

https://www.bilibili.com/video/BV1HP4118797/

## Container

不是小型虚拟机。

本质：
- process
- namespace
- cgroup
- layered filesystem

---

## Volume

数据库数据不能只在 writable layer。

否则容器删除：

```text
数据一起没
```

Postgres 必须 volume。

---

## Network

Compose 中：

```text
backend → postgres:5432
```

不是：

```text
backend → 127.0.0.1:5432
```

localhost 指：
- 当前容器自己

这个问题会成为你最终 Agent Demo 的真实故障案例之一。

---

## Linux 排错

```bash
top
free -h
df -h
ss -lntp
lsof -i :8080
docker logs
journalctl
```

面试不在于背命令，而是按层排：

```text
进程
→ 端口
→ 网络
→ 配置
→ 日志
→ 下游
```

---

# Week 6：Go

官方：

https://go.dev/doc/go1.27

## goroutine

轻量，不等于免费。

100 万 goroutine 仍可能：
- 内存
- scheduler
- 下游连接
- fd
- rate limit
爆掉。

---

## channel

用于：
- communication
- synchronization
- semaphore

例如并发限制：

```go
sem := make(chan struct{}, 20)

sem <- struct{}{}
defer func() { <-sem }()
```

最多 20 个并发。

---

## context

LLM Gateway 必须：

```text
client canceled
→ gateway canceled
→ provider request canceled
```

否则用户走了还在烧 Token。

---

# Week 7：LLM Tool Calling

## Tool Calling 本质

模型输出：

```text
我建议调用某 tool + arguments
```

真正执行：
- 你的程序

因此 Agent 安全边界绝不能交给 Prompt。

---

## Schema

Tool：

```text
search_official_docs(
  query: string,
  product: enum[docker,kubernetes],
  top_k: integer
)
```

比：

```text
search(input:any)
```

稳定得多。

---

## Side Effect

像：

```text
create_ticket
restart_service
refund
```

必须：
- authorization
- idempotency
- audit
- human approval

---

# Week 8：RAG

LangChain：

https://www.bilibili.com/video/BV1HgK86zEzr/

## RAG 两段

```text
Retrieval
+
Generation
```

这两个错误必须分开评测。

### Retrieval 错

正确文档没召回。

看：
- chunk
- query
- embedding
- metadata
- top-k
- hybrid
- rerank

### Generation 错

文档已经在 context，但答案错。

看：
- prompt
- context order
- model
- conflicting evidence
- too much noise

---

## 官方文档 vs Issue

最重要的项目设计之一：

```text
official_doc
```

表示：
- 主要事实证据

```text
github_issue
```

表示：
- 历史案例
- 不等于官方结论

```text
stackoverflow
```

表示：
- 社区参考

回答时必须保留 source_type。

---

## Chunk

对技术文档不要粗暴固定字符截断。

至少：
1. heading split
2. 长段再切
3. 保留 code blocks
4. metadata 继承

---

# Week 9：LangGraph

视频：

https://www.bilibili.com/video/BV1mrdwBtEEH/

## 为什么 Graph

复杂 Agent 有：
- branch
- retry
- human approval
- fallback
- checkpoint

while loop 很快会不可维护。

Graph 把：
- state
- node
- edge
显式化。

---

## Checkpoint

长任务执行一半崩溃：

没有 checkpoint：
- 从头跑

有：
- 恢复

但 Node 有副作用时，还必须：
- 幂等

否则恢复会重复：
- 创建 Ticket
- 发消息

---

# Week 10：MCP / Spring AI

MCP 2026：

https://blog.modelcontextprotocol.io/posts/2026-07-28/

## MCP

不是：
- 新的 LLM
- 新的 RAG
- 替代 REST

它是：
- AI 应用与能力提供者之间的标准协议

Server 暴露：
- Tools
- Resources
- Prompts

---

## Transport

官方规范：

https://github.com/modelcontextprotocol/modelcontextprotocol/blob/main/docs/specification/2026-07-28/basic/transports/index.mdx

常见：
- stdio
- Streamable HTTP

---

## 为什么不能 expose execute_sql

因为模型会获得：
- 任意查询
- 可能越权
- 修改
- 删除
- 数据泄露

更安全：

```text
get_ticket
search_issues
get_user_open_tickets
```

业务语义 Tool。

---

## Spring AI

官方：

https://docs.spring.io/spring-ai/reference/

Spring AI 的价值：
- Java 企业系统直接接模型
- Tool
- Memory
- RAG
- MCP
- Advisors

不是说 Python 不需要。

合理架构：

```text
Java：稳定业务
Python：复杂 Agent workflow
```

---

# Week 11：Evaluation / Reliability

## Reliability

外部依赖：

```text
LLM
Embedding
Postgres
Redis
GitHub
MCP
```

都会失败。

所以：

```text
timeout
retry
backoff
circuit breaker
fallback
rate limit
```

---

## Retry

适合：
- 429
- temporary 5xx
- transient network

不适合：
- invalid arguments
- unauthorized
- business rejection

Side-effect Tool 重试必须：
- idempotency key

---

## Evaluation

至少分：

```text
retrieval
tool
workflow
answer
```

不要只做一个“LLM-as-judge 总分”。

### Retrieval

```text
Hit@K
MRR
```

### Tool

```text
Tool Selection Accuracy
Argument Accuracy
Tool Success Rate
```

### Answer

```text
Expected Facts Pass Rate
Citation Accuracy
```

---

# Week 12：项目深挖与综合面试

## 项目介绍不要堆技术名

错误：

> 我用了 Spring Boot、Redis、LangGraph、MCP、Go……

正确：

> 我做了一个研发支持 Agent。用户提交 Docker/Kubernetes 故障问题后，系统优先检索版本可追踪的官方文档，再检索公开历史 Issue 作为案例，必要时通过受限 MCP Tool 查询工单或服务状态；Java 负责工单、权限和知识源管理，Python LangGraph 负责 Agent 流程，Go Gateway 统一模型调用，最终对 Retrieval、Tool、Workflow 和延迟分别做评测。

---

# 高频综合场景题

## 1. RAG 回答错了怎么查？

```text
先看正确 source 是否进 Top-K
```

没进：
- retrieval

进了但没回答：
- generation

---

## 2. GitHub Issue 与官方文档冲突怎么办？

优先：
- 官方文档

Issue：
- 标为历史案例
- 显示日期 / repo / state

不要让 Agent 自己把 Issue 升格为官方政策。

---

## 3. 用户要求 Agent 重启线上服务

不能直接执行。

流程：

```text
authorize
→ check environment
→ prepare action
→ human approval
→ idempotency
→ execute
→ audit
```

---

## 4. LLM API P99 20 秒

考虑：

```text
TTFT
Streaming
Timeout
模型路由
fallback
减少 context
cache
并行 retrieval
```

不是只说：
- 加服务器

---

# 每周复习法

Day 6：

1. 不看笔记讲 5 个概念
2. 5 道知识题
3. 3 道场景题
4. 从项目代码里找对应实现
5. 所有答不出的写：

```text
docs/interview/wrong-answers.md
```

目标：

```text
不是“我看过”
而是“我能结合自己的项目回答”
```