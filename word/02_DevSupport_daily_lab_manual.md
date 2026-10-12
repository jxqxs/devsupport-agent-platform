# 02｜DevSupport Agent Platform：逐日实验任务书

> 这是每天最常打开的文档。
>
> 每个 Day 固定结构：
>
> **问题 → 前置知识 → 命令 → 文件 → 代码 → 运行 → 预期现象 → 为什么 → 故意制造错误 → 项目映射 → 验收**
>
> 默认环境：
>
> ```text
> Cursor Remote SSH
> → Ubuntu 24.04
> → ~/projects/devsupport-agent-platform
> ```

# Week 1 Day 1｜创建项目 + equals/hashCode

## 今天解决什么问题

- 服务器项目怎么建
- Java 对象“相等”到底是什么意思
- HashSet / HashMap 为什么依赖 equals/hashCode

## 创建总仓库

```bash
mkdir -p ~/projects
cd ~/projects
mkdir -p devsupport-agent-platform
cd devsupport-agent-platform
git init

mkdir -p   backend-java   agent-python   llm-gateway-go   mcp-servers   data/raw   data/processed   data/eval   scripts   deploy   docs/experiments   docs/interview   docs/architecture   docs/data-license
```

## 创建 Maven Lab

```bash
cd backend-java

mvn archetype:generate   -DgroupId=com.example.devsupport   -DartifactId=java-basics-lab   -DarchetypeArtifactId=maven-archetype-quickstart   -DinteractiveMode=false

cd java-basics-lab
mvn clean compile
```

## Ticket.java

路径：

```text
src/main/java/com/example/devsupport/model/Ticket.java
```

```java
package com.example.devsupport.model;

import java.util.Objects;

public class Ticket {
    private Long id;
    private String title;

    public Ticket(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ticket other)) {
            return false;
        }
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Ticket{id=" + id
                + ", title='" + title + "'}";
    }
}
```

## ObjectDemo.java

```java
package com.example.devsupport;

import com.example.devsupport.model.Ticket;
import java.util.HashSet;
import java.util.Set;

public class ObjectDemo {

    public static void main(String[] args) {

        Ticket t1 = new Ticket(
                1L,
                "Docker cannot connect postgres"
        );

        Ticket t2 = new Ticket(
                1L,
                "Same business ticket"
        );

        System.out.println(
                "== : " + (t1 == t2)
        );

        System.out.println(
                "equals : " + t1.equals(t2)
        );

        Set<Ticket> set = new HashSet<>();
        set.add(t1);
        set.add(t2);

        System.out.println(
                "set size = " + set.size()
        );
    }
}
```

运行：

```bash
mvn clean compile

java   -cp target/classes   com.example.devsupport.ObjectDemo
```

预期：

```text
== : false
equals : true
set size = 1
```

## 为什么

```text
==        → 是否同一对象
equals    → 业务逻辑相等
hashCode  → Hash 容器定位桶
```

## 故意制造错误

临时删掉 `hashCode()`，重新跑，观察 HashSet 行为。

## 项目映射

以后 Ticket、KnowledgeSource、Document metadata 都必须明确“唯一身份”。

## 当天验收

能口述：

- `==`
- `equals`
- `hashCode`
- 为什么 key 最好不可变

---

# Week 1 Day 2｜泛型 + Result<T>

## 今天解决什么问题

后端 API 为什么不能全部返回 Object。

## Result.java

```java
package com.example.devsupport.common;

public class Result<T> {

    private final int code;
    private final String message;
    private final T data;

    private Result(
            int code,
            String message,
            T data
    ) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T>
    Result<T> success(T data) {

        return new Result<>(
                0,
                "success",
                data
        );
    }

    public static <T>
    Result<T> fail(
            int code,
            String message
    ) {
        return new Result<>(
                code,
                message,
                null
        );
    }

    public T getData() {
        return data;
    }
}
```

测试：

```java
Result<Ticket> r1 =
        Result.success(ticket);

Result<String> r2 =
        Result.success("ok");
```

## 知识到代码的桥梁

```text
泛型
→ Result<T>
→ 后面的 ApiResponse<T>
→ 类型安全的 Spring API
```

---

# Week 1 Day 3｜ArrayList / LinkedList / HashSet

## 任务

分别构造 100000 个元素，比较随机读取耗时。

再做：

```text
HashSet<Ticket>
```

去重实验。

## 必须记录

- ArrayList `get(i)` 为什么快
- LinkedList 为什么随机访问慢
- HashSet 为什么依赖 hashCode/equals

产出：

```text
docs/experiments/week01-list-set.md
```

---

# Week 1 Day 4｜HashMap 可变 Key

写 `MutableKey`：

```java
class MutableKey {
    String value;

    // equals/hashCode 都基于 value
}
```

流程：

```text
put(key)
↓
修改 key.value
↓
get(key)
```

观察为什么可能查不到。

## 项目映射

Map key 优先用：

```text
Long
String
UUID
不可变 Value Object
```

---

# Week 1 Day 5｜异常体系

创建：

```text
ErrorCode
BusinessException
```

至少：

```text
TICKET_NOT_FOUND
KNOWLEDGE_SOURCE_NOT_FOUND
INVALID_PARAMETER
```

实验：

```text
Service 找不到 Ticket
→ throw BusinessException
```

不要：

```text
return null
```

---

# Week 1 Day 6｜Stream + 周复盘

用 Stream 完成：

- 过滤 OPEN Ticket
- 按 status 分组
- 统计每类数量
- 提取 title
- 排序

周末脱离文档重新写：

```text
Ticket
Result<T>
BusinessException
```

---

# Week 2 Day 1｜竞态条件 / synchronized / AtomicInteger

## UnsafeCounterDemo

```java
package com.example.devsupport.concurrent;

public class UnsafeCounterDemo {

    private static int count = 0;

    public static void main(String[] args)
            throws InterruptedException {

        int threadCount = 100;
        int loopCount = 10_000;

        Thread[] threads =
                new Thread[threadCount];

        for (int i = 0;
             i < threadCount;
             i++) {

            threads[i] =
                    new Thread(() -> {

                for (int j = 0;
                     j < loopCount;
                     j++) {

                    count++;
                }
            });

            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        System.out.println(
                "expected="
                + threadCount * loopCount
        );

        System.out.println(
                "actual=" + count
        );
    }
}
```

运行：

```bash
mvn clean compile

java   -cp target/classes   com.example.devsupport.concurrent.UnsafeCounterDemo
```

通常：

```text
actual < expected
```

## 为什么

```text
count++
=
read
+
add
+
write
```

不是原子操作。

## synchronized 版本

用固定 `LOCK` 包住 `count++`。

预期：

```text
1000000
```

## AtomicInteger

```java
private static final
AtomicInteger count =
        new AtomicInteger();

count.incrementAndGet();
```

## 今日知识链

```text
Race Condition
↓
Atomicity
↓
synchronized
↓
CAS
↓
AtomicInteger
```

## 项目映射

- 请求计数
- Metrics
- 轻量并发状态

## 产出

```text
docs/experiments/week02-counter.md
```

---

# Week 2 Day 2｜ThreadPoolExecutor

## 先做串行版本

100 个 Ticket。

每个：

```java
Thread.sleep(100);
```

模拟外部查询。

总耗时大约：

```text
10s
```

## 再做线程池

```java
ThreadPoolExecutor executor =
        new ThreadPoolExecutor(
                4,
                8,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100),
                Executors
                        .defaultThreadFactory(),
                new ThreadPoolExecutor
                        .CallerRunsPolicy()
        );
```

使用：

```java
CountDownLatch latch =
        new CountDownLatch(100);
```

等待所有任务完成。

## 故意制造拒绝

改成：

```text
core = 2
max = 4
queue = 2
```

提交 100 个任务。

观察 `CallerRunsPolicy`。

## 今日知识链

```text
任务很多
↓
不能无限开线程
↓
core
↓
queue
↓
max
↓
reject
```

## 项目映射

- 批量文档处理
- 异步审计
- 外部调用隔离
- 后续并行检索基础

---

# Week 2 Day 3｜CompletableFuture / ThreadLocal / ReentrantLock

## CompletableFuture

模拟：

```text
searchDockerDocs() 500ms
searchGithubIssues() 700ms
```

先串行，再并行：

```java
CompletableFuture<String> docs =
    CompletableFuture.supplyAsync(
        () -> searchDockerDocs()
    );

CompletableFuture<String> issues =
    CompletableFuture.supplyAsync(
        () -> searchGithubIssues()
    );

CompletableFuture.allOf(
    docs,
    issues
).join();
```

比较耗时。

## ThreadLocal

模拟：

```text
requestId
```

在线程池任务里：

```java
try {
    threadLocal.set(requestId);
} finally {
    threadLocal.remove();
}
```

## ReentrantLock

练：

```java
tryLock()
```

理解：

```text
synchronized 简单
Lock 更灵活
```

---

# Week 2 Day 4｜JVM / GC / Arthas

## OOM

用小堆运行：

```bash
java -Xms32m -Xmx32m ...
```

程序不断创建大数组。

观察：

```text
OutOfMemoryError
```

## StackOverflow

递归：

```java
static void f() {
    f();
}
```

观察：

```text
StackOverflowError
```

## Arthas

练：

```text
dashboard
thread
jad
trace
```

产出：

```text
docs/experiments/week02-jvm.md
```

---

# Week 2 Day 5｜MySQL 索引实验准备

启动：

```bash
docker run -d   --name mysql8   -e MYSQL_ROOT_PASSWORD=123456   -p 3306:3306   mysql:8
```

建表：

```sql
CREATE DATABASE devsupport_lab;

USE devsupport_lab;

CREATE TABLE support_tickets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(64),
    status VARCHAR(32),
    priority VARCHAR(16),
    created_at DATETIME NOT NULL
);
```

写 Python 脚本生成 10 万条“数据库压测用虚拟工单”。

注意：

> 这些不是知识库真实工单，只用于索引实验。

---

# Week 2 Day 6｜EXPLAIN + 联合索引

基准 SQL：

```sql
SELECT *
FROM support_tickets
WHERE status='OPEN'
ORDER BY created_at DESC
LIMIT 20;
```

依次比较：

```text
1. 无索引
2. INDEX(status)
3. INDEX(status, created_at)
```

记录：

```text
type
key
rows
Extra
```

产出：

```text
docs/experiments/week02-mysql-index.md
```

---

# Week 3｜Spring Boot 每日实现

## Day 1：创建项目

Spring Initializr：

```text
Maven
Java 21
Spring Boot 4.1.x
Group: com.example
Artifact: devsupport-backend
```

路径：

```text
~/projects/devsupport-agent-platform/backend-java/devsupport-backend
```

启动：

```bash
./mvnw spring-boot:run
```

写：

```text
GET /health
```

用 curl 验证。

## Day 2：Ticket CRUD

按顺序创建：

```text
entity/Ticket
repository/TicketRepository
service/TicketService
controller/TicketController
dto/CreateTicketRequest
vo/TicketVO
```

接口：

```text
POST /api/tickets
GET /api/tickets/{id}
GET /api/tickets
PATCH /api/tickets/{id}/status
```

## Day 3：PostgreSQL

启动 Postgres。

建：

```text
users
tickets
ticket_comments
```

将内存 Repository 换 DB。

## Day 4：Validation + GlobalExceptionHandler

故意发送：

```text
空 title
非法 priority
不存在 id
```

确认错误结构统一。

## Day 5：KnowledgeSource API

增加：

```text
sourceType
product
sourceUrl
license
version
```

## Day 6：全链路测试

完成：

```text
创建 Ticket
→ 查询
→ 更新状态
→ 添加评论
```

---

# Week 4｜Redis / JWT

## Day 1

启动 Redis。

先在 redis-cli 手工练：

```text
SET
GET
EXPIRE
HSET
TTL
```

再接 `StringRedisTemplate`。

## Day 2

模拟热点 key 过期：

```text
100 并发
→ 全部 miss
→ 模拟 DB 500ms
```

记录 DB hit 次数。

再加 mutex rebuild。

## Day 3

先实现 JWT：

```text
generate
parse
validate
```

再做：

```text
Access 30min
Refresh 7d
```

## Day 4

注册 / 登录：

```text
BCrypt
password hash
```

## Day 5

RBAC：

```text
USER
SUPPORT
ADMIN
```

## Day 6

Refresh Token 写 Redis。

测试：

```text
login
→ refresh
→ logout
→ refresh 失败
```

---

# Week 5｜Docker / Linux / Nginx

## Day 1

练：

```bash
top
free -h
df -h
ss -lntp
lsof -i :8080
docker logs
journalctl
```

## Day 2

Postgres 用 volume。

删容器重建，确认数据仍存在。

## Day 3

故意：

```text
DB_HOST=127.0.0.1
```

观察容器连接失败。

改：

```text
DB_HOST=postgres
```

恢复。

这个 Case 保留为最终 Agent Demo。

## Day 4

写 Spring Boot Dockerfile。

## Day 5

Compose：

```text
backend
postgres
redis
```

## Day 6

Nginx 反代 `/api/`。

故意配错 upstream 制造 502，再排查。

---

# Week 6｜Go LLM Gateway

- Day 1：module、struct、interface、error
- Day 2：goroutine、channel、WaitGroup
- Day 3：context cancellation；MockProvider sleep 10s，timeout 2s
- Day 4：Gin `/health` `/models` `/chat`
- Day 5：Provider Interface、MockProvider、OpenAI-compatible
- Day 6：retry、semaphore 限流、streaming、request_id

每个 Day 必须：

```text
编译
+
curl
+
故意制造一次错误
```

---

# Week 7｜Python Agent + 真实数据

- Day 1：`python3 -m venv .venv`；FastAPI `/health`
- Day 2：直接调用 LLM API；普通 + Streaming
- Day 3：Pydantic Structured Output
- Day 4：不用 LangChain，手写 Tool Calling Loop
- Day 5：clone Docker / Kubernetes docs，记录 commit/license
- Day 6：GitHub Issues REST API → JSONL，过滤 PR

Tool 至少：

```text
calculator
get_ticket
search_official_docs
```

限制：

```text
MAX_TOOL_STEPS=5
```

---

# Week 8｜RAG

- Day 1：真实 docs / issue 归一化 schema
- Day 2：Markdown Heading-aware Chunk
- Day 3：Embedding + pgvector insert
- Day 4：semantic_search + metadata filter
- Day 5：Postgres FTS + vector Hybrid Search
- Day 6：100 条 Eval；Hit@5 / MRR

---

# Week 9｜LangGraph

- Day 1：raw loop → State / Node / Edge
- Day 2：Router：docker / k8s / ticket / general
- Day 3：并行 official docs / issues / community retrieval
- Day 4：ReAct Tool Loop 接 graph
- Day 5：Source Verifier；max retry=2
- Day 6：Checkpoint + HITL；故意中途崩溃再恢复

---

# Week 10｜MCP + Spring AI

- Day 1：跑 MCP Quickstart，理解 Host / Client / Server
- Day 2：document-mcp
- Day 3：issue-mcp
- Day 4：ticket-mcp；写操作要求 approval
- Day 5：Spring AI ChatClient / Tool / Memory
- Day 6：同一能力分别用 Python LangGraph 与 Spring AI 实现并比较

---

# Week 11｜Eval / Trace / Reliability

- Day 1：全链路 request_id + Trace JSON
- Day 2：Retrieval Eval
- Day 3：Tool Selection / Argument Eval
- Day 4：Answer Facts / Citation Accuracy
- Day 5：timeout / retry / fallback
- Day 6：故障注入

故障至少：

```text
LLM timeout
Redis down
Postgres down
MCP down
Empty retrieval
Tool 500
```

每个故障写：

```text
现象
→ 日志
→ 定位
→ 根因
→ 修复
→ 预防
```

---

# Week 12｜最终成品

- Day 1：README、架构图
- Day 2：准备 5 个真实 Demo
- Day 3：整理 Eval 和延迟数据
- Day 4：项目追问题库
- Day 5：Docker Compose 一键启动、GitHub 清理
- Day 6：Mock Interview、简历项目描述

---

# 每天固定结束动作

```bash
git status
git add .
git commit -m "weekX dayY: ..."
```

并写：

```text
docs/weekXX-dayYY.md
```

最少包含：

```text
今天完成
实验结果
遇到的 Bug
原因
怎么修
对应知识点
对应面试题
```
