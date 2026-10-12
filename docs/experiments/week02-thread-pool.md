# Week 2 线程池实验

类都在 `com.example.devsupport.juc`。编译后分别运行：

```bash
cd backend-java/java-basics-lab
mvn -q -DskipTests compile
java -cp target/classes com.example.devsupport.juc.TicketSerialDemo
java -cp target/classes com.example.devsupport.juc.TicketPoolDemo
```

## 串行处理 100 个工单

`TicketSerialDemo` 准备 100 个 `Ticket`，每个 `Thread.sleep(100)`，模拟一次外部查询。

```text
serial tickets=100, elapsed=10018ms
```

100 次查询排在同一条线程上，总时间大约是 `100 × 100ms`。

## 线程池处理

`TicketPoolDemo` 用 `ThreadPoolExecutor`：核心线程 4，最大线程 8，空闲存活 60 秒，队列 `ArrayBlockingQueue(100)`，拒绝策略 `CallerRunsPolicy`。100 个任务提交后，用 `CountDownLatch(100)` 等到全部 `countDown`。

```text
test1(4, 8, 100)
pool tickets=100, callerRuns=0, elapsed=2507ms
```

先占满 4 个核心线程，其余任务进入容量 100 的队列。队列没有满，最大线程数没有被用到，`CallerRunsPolicy` 也没有接手任务，所以 `callerRuns = 0`。4 个线程同时做 100ms 的查询，总时间大约是串行的四分之一。

## 小池子上的 CallerRunsPolicy

同一段代码再跑一组参数：核心线程 2，最大线程 4，队列容量 2，仍然提交 100 个任务。`main` 开头的输出是提交线程自己执行的工单。

```text
test1(2, 4, 2)
pool tickets=100, callerRuns=30, elapsed=3021ms
```

这条路径是：核心线程接满，再进队列，队列满了把线程加到最大数，还放不下就拒绝。`CallerRunsPolicy` 不抛异常，被拒绝的任务由调用 `submit` 的 `main` 线程执行。这次有 30 个任务落在 `main` 上。`main` 自己睡 100ms 时，池里的线程会把队列腾出来，所以后面又有任务能进池子，`callerRuns` 每次运行都会变。
