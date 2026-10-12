# 03｜DevSupport Agent Platform：知识点教程 + 面试题详解

> 这份文档只负责：
>
> ```text
> 为什么
> 原理
> 机制
> 面试
> ```
>
> 不负责逐步告诉你今天敲什么命令。
>
> 每个知识点都标：
>
> ```text
> 对应实验
> 对应主项目
> 对应面试题
> ```

# Week 1｜Object / equals / hashCode

## 核心概念

```text
==
→ 判断是不是同一个对象

equals
→ 判断业务逻辑上是否相同

hashCode
→ 帮 Hash 容器快速定位
```

## 最重要契约

```text
equals 相等
→ hashCode 必须相等
```

但：

```text
hashCode 相等
↛ equals 一定相等
```

因为哈希值可能冲突。

## 为什么可变 key 危险

插入时：

```text
hash("alice")
→ bucket 3
```

修改对象后：

```text
hash("bob")
→ bucket 7
```

对象仍在 bucket 3。

于是：

```text
get(key)
```

可能去 bucket 7 找，最终失败。

## 对应实验

```text
02 逐日实验任务书
Week 1 Day 1 / Day 4
```

## 对应主项目

```text
Ticket
KnowledgeSource
Document metadata
```

## 面试题

### Q：为什么 equals 和 hashCode 要一起重写？

答：

HashMap / HashSet 先根据 hashCode 定位桶，再在桶内通过 equals 判断逻辑相等。如果两个 equals 相等的对象 hashCode 不同，它们可能进入不同桶，集合就会把逻辑上相同的对象当成不同对象。

---

# Week 1｜泛型

## 为什么需要泛型

没有泛型：

```java
List list = new ArrayList();

list.add("hello");
list.add(123);
```

调用时：

```java
String s = (String) list.get(1);
```

运行期才报错。

泛型：

```java
List<String> list =
        new ArrayList<>();
```

编译器直接阻止：

```java
list.add(123);
```

所以泛型的核心价值：

> 把类型错误从运行期提前到编译期。

## Result<T>

```java
Result<Ticket>
Result<List<Ticket>>
Result<String>
```

同一结构可承载不同 data 类型。

## 类型擦除

Java 泛型主要是编译期机制。

运行时：

```text
List<String>
List<Integer>
```

很多情况下都只剩：

```text
List
```

所以复杂 JSON 反序列化有时需要额外 Type 信息。

## 对应实验

```text
Week 1 Day 2
```

## 面试题

### Q：为什么不用 Object？

答：

Object 会丢失编译期类型安全和 IDE 类型推断，调用方需要强制转换，很多错误只能等运行时才暴露。泛型能让接口表达更清晰。

---

# Week 1｜ArrayList / LinkedList

## ArrayList

底层：

```text
动态数组
```

特点：

- 随机访问 O(1)
- 尾部追加通常快
- 中间插入/删除需要移动元素
- 内存局部性较好

## LinkedList

底层：

```text
双向链表
```

特点：

- 已知节点时插入删除方便
- 随机访问 O(n)
- 节点有 prev / next 额外开销
- 缓存局部性差

## 实际选择

大多数普通后端业务：

> 优先 ArrayList。

LinkedList 的实际使用频率通常没有面试题里那么高。

## 对应实验

```text
Week 1 Day 3
```

## 面试题

### Q：为什么 LinkedList 插入 O(1)，但实际不一定比 ArrayList 快？

答：

因为“插入 O(1)”通常假设你已经拿到目标节点。如果要先找第 i 个位置，LinkedList 本身要 O(n) 遍历；同时节点对象更多、缓存局部性较差。ArrayList 虽要搬移元素，但连续内存访问在实际机器上可能很高效。

---

# Week 1｜HashMap

## JDK 8 典型结构

```text
数组
↓
桶
↓
链表
↓
必要时红黑树
```

## put 流程

```text
hash
↓
index = (n - 1) & hash
↓
桶为空？
├─ 是 → 直接插
└─ 否
   ↓
   key 是否相同？
   ├─ 是 → 更新
   └─ 否 → 链表 / 红黑树查找
↓
size 是否超阈值
↓
resize
```

## 为什么容量常为 2 的幂

因为：

```java
(n - 1) & hash
```

可快速完成桶定位，也让扩容时元素迁移规律更简单。

## 负载因子 0.75

这是：

```text
空间
vs
冲突概率
```

的工程折中，不是数学定律。

## 对应实验

```text
Week 1 Day 4
```

## 场景题

### Q：ConcurrentHashMap 能保证用户名唯一吗？

答：

不能。线程安全只说明单个 Map 操作安全，不代表：

```java
containsKey()
+
put()
```

这个组合天然原子。

可以：

```java
putIfAbsent()
```

但真正业务落数据库后仍应依赖：

```text
UNIQUE INDEX
+
事务 / 幂等
```

---

# Week 1｜Exception

## Throwable 体系

```text
Throwable
├── Error
└── Exception
    └── RuntimeException
```

Error 通常表示 JVM / 系统级严重问题。

Exception 表示程序可以处理、转换或上抛的异常情况。

## Checked Exception

编译器要求：

```text
catch
或
throws
```

## RuntimeException

编译器不强制显式处理。

业务异常常继承 RuntimeException：

```java
BusinessException
```

原因：

- 不需要层层写 throws
- 可统一交给 GlobalExceptionHandler
- Spring 默认事务回滚语义更自然

## 最危险错误

```java
try {
    ...
} catch (Exception e) {
}
```

这是吞异常。

后果：

```text
错误发生
↓
调用方不知道
↓
日志可能没有
↓
数据可能处于半成功状态
```

## 对应实验

```text
Week 1 Day 5
```

---

# Week 2｜Race Condition / CAS / AtomicInteger

## Race Condition

多个线程操作共享状态，结果依赖执行顺序。

## 为什么 count++ 不安全

```text
count++
=
read
+
add
+
write
```

线程 A/B 都可能读到相同旧值，导致丢更新。

## synchronized

主要解决：

```text
互斥
+
可见性
```

同一时刻只允许一个线程进入临界区。

## CAS

Compare-And-Set：

```text
如果当前值 == expected
→ 写 newValue

否则
→ 失败 / 重试
```

AtomicInteger 常基于 CAS。

## ABA

```text
A → B → A
```

最终值又变回 A，普通 CAS 只看当前值，不知道中间变化。

可通过：

```text
value + version
```

思路识别。

## 对应实验

```text
Week 2 Day 1
```

## 对应主项目

```text
request counter
metrics
轻量共享状态
```

## 面试题

### Q：volatile 为什么不能代替 AtomicInteger？

答：

volatile 保证可见性以及一定的有序性，但不会把 read-modify-write 复合操作变成原子操作。`i++` 仍可能在多个线程间丢失更新。

---

# Week 2｜ThreadPoolExecutor

## 七参数

```text
corePoolSize
maximumPoolSize
keepAliveTime
unit
workQueue
threadFactory
rejectedExecutionHandler
```

## 提交顺序

```text
core 未满
→ 创建核心线程

core 满
→ 进入 queue

queue 满
→ 扩容线程到 max

max 也满
→ reject
```

## 为什么线程不是越多越快

线程太多会带来：

- Context Switch
- 线程栈内存
- DB 连接池耗尽
- HTTP 连接耗尽
- 下游服务被压垮
- 调度开销

## CPU-bound vs IO-bound

CPU-bound：

```text
纯计算
```

线程数通常不宜远高于 CPU 核心。

IO-bound：

```text
DB
HTTP
LLM
File IO
```

等待时间多，可适度增加线程。

但仍受：

```text
connection pool
rate limit
memory
downstream capacity
```

约束。

## 对应实验

```text
Week 2 Day 2
```

## 场景题

### Q：线程池 Queue 持续增长怎么办？

答题顺序：

```text
任务生产速度
↓
任务平均耗时
↓
active threads
↓
queue size
↓
下游 latency
↓
DB / HTTP connection pool
↓
timeout
↓
是否出现慢任务
```

不能第一反应只是：

> 把 maximumPoolSize 调大。

---

# Week 2｜CompletableFuture / ThreadLocal / ReentrantLock

## CompletableFuture

适合表达：

```text
多个异步任务
+
组合
```

例如：

```text
Official Docs Search
和
GitHub Issue Search
```

可以同时执行。

## ThreadLocal

每个线程持有一份自己的变量。

常见：

```text
request context
trace id
```

在线程池场景一定注意：

```java
finally {
    threadLocal.remove();
}
```

否则线程复用后可能出现上下文污染。

## ReentrantLock

与 synchronized 都可互斥。

它额外提供：

```text
tryLock
lockInterruptibly
fair lock
Condition
```

复杂控制更灵活，但需要手工 unlock。

## 对应实验

```text
Week 2 Day 3
```

---

# Week 2｜JVM

## Heap

主要存：

```text
对象
数组
```

## Stack

每个线程私有。

每次方法调用创建栈帧。

## Metaspace

主要存类元数据。

## GC

HotSpot 主流使用：

```text
可达性分析
```

从 GC Roots 出发，能到达的是活对象。

常见 Roots：

- 栈引用
- static 引用
- JNI 引用
- 锁对象

## STW

Stop-The-World：

```text
应用线程暂停
```

所以线上要看：

```text
P95
P99
GC Pause
```

而不是只看平均延迟。

## 对应实验

```text
Week 2 Day 4
```

## 场景题

### Q：Java CPU 100% 怎么查？

答：

```text
top 找进程 PID
↓
top -Hp PID 找线程
↓
线程 ID 转十六进制
↓
jstack / Arthas thread
↓
定位栈
```

再判断：

- 无限循环
- GC
- 锁竞争
- busy loop
- regex
- JSON/序列化热点

---

# Week 2｜MySQL 索引

## 为什么 B+Tree

数据库真正昂贵的是：

```text
磁盘 IO
```

B+Tree 一个节点可以容纳很多 key，树高度低，范围查询也方便。

## 聚簇索引

InnoDB 主键索引叶子：

```text
完整行数据
```

## 二级索引

叶子通常：

```text
secondary key
+
primary key
```

## 回表

二级索引拿到主键后：

```text
再查主键索引
```

拿完整行。

## 覆盖索引

查询所需列都已在索引中：

```text
无需回表
```

## 联合索引

索引：

```text
(status, created_at)
```

不是两个完全独立索引字段，而是：

```text
先 status 排
status 相同再 created_at 排
```

所以顺序与真实查询有关。

## 对应实验

```text
Week 2 Day 5~6
```

## 场景题

SQL：

```sql
SELECT *
FROM support_tickets
WHERE status='OPEN'
ORDER BY created_at DESC
LIMIT 20;
```

很慢怎么办？

答：

```text
EXPLAIN
↓
看数据量
↓
看 status 分布
↓
看当前索引
↓
是否需要联合索引
↓
ORDER BY 能否利用索引
↓
是否回表过多
↓
实际压测
```

不要只回答：

> 给 status 建索引。

---

# Week 3｜Spring

## IoC

对象的：

```text
创建
组装
生命周期
```

交给 Spring 容器管理。

## DI

依赖由容器注入。

推荐：

```text
构造器注入
```

优点：

- 依赖显式
- 可以 final
- 易测试
- 不会隐藏依赖

## Spring MVC 请求链

```text
Client
↓
Filter
↓
DispatcherServlet
↓
HandlerMapping
↓
Controller
↓
Service
↓
Repository
↓
Response
```

## DTO / VO / Entity

```text
Entity
→ 数据库存储

DTO
→ 请求输入

VO
→ 响应输出
```

因为：

```text
DB Schema
≠
API Contract
```

## @Transactional

常见问题：

- 同类自调用
- 异常被 catch 后不抛
- 事务范围过大
- 外部网络调用放在事务里

## 对应实验

```text
Week 3
Ticket CRUD
Validation
GlobalExceptionHandler
```

## 场景题

### Q：为什么 LLM API 调用不应该包在数据库长事务里？

答：

LLM 请求可能等待几秒甚至几十秒。如果事务一直持有数据库连接或锁，会降低吞吐并增加锁竞争。应缩短事务范围，把外部调用放事务外，通过状态机、异步任务或补偿保证业务一致性。

---

# Week 4｜Redis / JWT

## Redis 为什么快

不仅是“单线程”。

还包括：

```text
内存访问
高效数据结构
事件循环
IO 多路复用
较少锁竞争
```

## 缓存穿透

```text
查询不存在的数据
→ Cache Miss
→ DB Miss
```

## 缓存击穿

```text
一个热点 Key 过期
→ 大量并发瞬间打 DB
```

## 缓存雪崩

```text
大量 Key 同时过期
或
Redis 整体故障
```

## JWT

结构：

```text
header.payload.signature
```

签名代表：

```text
内容没有被篡改
```

不代表：

```text
payload 被加密
```

## Access + Refresh

```text
Access Token
→ 生命周期短

Refresh Token
→ 生命周期长
```

## 对应实验

```text
Week 4
```

## 场景题

### Q：用户被封禁，但 Access Token 还有 20 分钟，要求立刻生效怎么办？

答：

纯无状态 JWT 无法即时撤销，需要增加服务端状态，例如：

```text
Redis blacklist
token version
server-side session
```

这是：

```text
无状态便利性
vs
即时控制能力
```

的 trade-off。

---

# Week 5｜Docker / Linux / Nginx

## Container

核心依赖：

```text
namespace
cgroup
layered filesystem
```

与 VM 不同：

```text
共享宿主机 Kernel
```

## Volume

数据库不能只依赖容器可写层。

删除容器后，数据应通过 Volume 持久化。

## localhost

Compose 场景：

```text
backend 容器中的 127.0.0.1
```

表示：

```text
backend 自己
```

不是 postgres。

正确：

```text
postgres:5432
```

## Nginx

可做：

- Reverse Proxy
- TLS
- Load Balance
- Rate Limit
- Static Files

## 对应实验

```text
Week 5 Day 3 / Day 6
```

## 场景题

### Q：Nginx 502 怎么查？

答：

```text
Nginx 是否正常
↓
upstream 配置
↓
Backend 是否运行
↓
端口是否监听
↓
网络 / DNS
↓
Backend 日志
```

---

# Week 6｜Go

## goroutine

由 Go Runtime 管理的轻量并发单元。

不是：

```text
1 goroutine = 1 OS Thread
```

## channel

用于：

```text
通信
同步
semaphore
```

## context

传播：

```text
deadline
cancel
request scoped values
```

## 对应实验

```text
Week 6 Day 3
```

## 场景题

### Q：用户取消请求，但 LLM Provider 仍跑 30 秒，为什么？

答：

说明 cancellation 没有从 HTTP Request Context 继续传播到 Provider 调用。整个调用链都应使用同一个父 Context 或派生 Context。

---

# Week 7｜Tool Calling

## 本质

模型做的是：

```text
选择 Tool
+
生成 Arguments
```

真正执行 Tool 的是：

```text
程序
```

所以必须有：

- 参数校验
- 权限
- Timeout
- Audit
- Retry 策略

## Side Effect Tool

例如：

```text
create_ticket
restart_service
```

必须考虑：

```text
Idempotency
Human Approval
Audit
Authorization
```

## 场景题

### Q：模型重复调用 create_ticket 两次怎么办？

答：

业务服务必须支持 idempotency key，同一个 Agent Run 中相同动作只能真正执行一次。不能依赖模型“通常不会重复调用”。

---

# Week 8｜RAG

## 两阶段

```text
Retrieval
+
Generation
```

一定要分开评测。

## Chunk

太小：

```text
语义碎
```

太大：

```text
噪声多
Token 浪费
```

技术文档建议：

```text
Heading-aware
+
保留 Code Block
+
保留 Metadata
```

## Hybrid Search

```text
Vector
+
Keyword
```

尤其适合：

- Error Code
- Class Name
- Command
- API Name

## Rerank

```text
粗召回较多
↓
精排
↓
只送前几条给模型
```

## 对应实验

```text
Week 8
```

## 场景题

### Q：正确文档已经在 Top-5，但模型回答错？

答：

说明 Retrieval 基本成功，应优先检查 Generation：

- Prompt
- Context 顺序
- 冲突证据
- Context 太长
- 模型能力
- 是否把历史 Issue 错当官方依据

---

# Week 9｜LangGraph

## 为什么 Graph

简单 Agent：

```text
while model → tool
```

够用。

但出现：

```text
分支
Retry
Verifier
HITL
Checkpoint
```

后，显式 Graph 更清晰。

## State

应该：

```text
小
可序列化
可持久化
```

不要放大文件、数据库连接等。

## Checkpoint

任务中途失败：

```text
从最近状态恢复
```

但副作用节点必须：

```text
幂等
```

否则恢复时可能重复创建工单。

## 对应实验

```text
Week 9
Router
Parallel Retrieval
Verifier
Checkpoint
```

---

# Week 10｜MCP / Spring AI

## MCP

MCP 用于标准化：

```text
AI 应用
如何发现 / 调用外部能力
```

Server 可暴露：

```text
Tools
Resources
Prompts
```

## MCP vs REST

MCP 不替代 REST。

MCP Server 内部完全可以再调用：

```text
REST
DB
SDK
```

## 为什么不暴露 execute_sql(sql)

风险：

- 越权
- 数据泄露
- 删除
- Prompt Injection
- 不可控修改

更好：

```text
get_ticket
search_issues
get_service_status
```

## Spring AI

价值：

```text
Java 企业系统
直接接 Chat / Tool / RAG / MCP
```

复杂 Agent Workflow 仍可以由 Python LangGraph 承担。

---

# Week 11｜Reliability / Evaluation

## Timeout

所有外部依赖都应该有上限。

## Retry

适合：

```text
429
临时性 5xx
网络抖动
```

不适合：

```text
非法参数
鉴权失败
业务拒绝
```

## Circuit Breaker

下游持续失败时：

```text
快速失败
↓
等待恢复
↓
再探测
```

## Trace

至少记录：

```text
request_id
retrieved docs
tool calls
model
tokens
latency
retry
error
```

## Eval

### Retrieval

```text
Hit@K
MRR
```

### Tool

```text
Tool Selection Accuracy
Argument Accuracy
Success Rate
```

### Answer

```text
Expected Fact Pass Rate
Citation Accuracy
```

### System

```text
P95
TTFT
Failure Rate
```

## 场景题

### Q：Overall Success Rate 95%，为什么用户仍投诉多？

答：

聚合指标可能掩盖问题。

应按：

```text
Intent
Source
Model
Tool
用户场景
```

分桶。

还要看：

```text
P95 / P99
```

而不是只看平均值和 Overall。

---

# Week 12｜项目表达

## 正确回答结构

```text
业务背景
↓
问题
↓
架构
↓
为什么这样设计
↓
关键实现
↓
怎么验证
↓
结果
↓
问题与改进
```

不要只念：

```text
Spring Boot
Redis
LangGraph
MCP
Go
```

---

# 每周错题本

统一维护：

```text
docs/interview/wrong-answers.md
```

每题写：

```text
问题
我的答案
哪里错
正确答案
项目中对应哪里
```
