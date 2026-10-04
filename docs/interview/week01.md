# Week 1 面试题复盘

## 1. equals / hashCode 契约

`equals` 为真时，`hashCode` 必须相同。`hashCode` 相同只表示可能落在同一个格子，还要用 `equals` 判断是不是同一个对象。

只重写 `equals`、不重写 `hashCode` 时，两个 `id` 相同的对象会进不同的格子。本周这样放进 `HashSet`，大小是 `2`。两个方法都按 `id` 写之后，大小是 `1`。

## 2. HashMap put 流程

先算哈希值，用 `(n - 1) & hash` 找到对应的桶。桶里用 `equals` 比较 key。没有相同 key 就写入，有就替换值。元素个数超过容量乘以负载因子 `0.75` 时扩容。

JDK 8 里，桶一开始是链表。链表长度超过 8，并且数组长度至少 64 时，转成红黑树。数组还没到 64 时先扩容，不树化。

## 3. 为什么 key 最好不可变

key 放入时按当时的 `hashCode` 进桶，之后字段变了，桶不会跟着改。例如原来的 key 是 `1`，改成 `2` 之后，再用这个对象去 `get`，会按 `2` 去另一个桶，原来的值就找不到。

再用一个新的、`id` 仍是 `1` 的对象去查，能走进原来的桶，但桶里对象的 `id` 已经是 `2`，`equals` 对不上，结果还是 `null`。所以用 `ticketId` 这种不会变的值当 key，不用可变对象。

## 4. ArrayList / LinkedList

用哪个取决于场景。`ArrayList` 按下标访问是 O(1)，中间插入或删除是 O(n)。`LinkedList` 找到第 i 个元素是 O(n)。已经拿到节点时，在该节点旁插入是 O(1)；按下标插入仍然要先走到那个位置。

链表节点还有前后指针，列表很长时更占内存。本周同一组随机下标：`ArrayList` 约 `1ms`，`LinkedList` 约 `936ms`。工单列表和知识来源列表默认用 `ArrayList`。

## 5. 泛型擦除

泛型主要在编译期起作用。运行时 `Result<Ticket>` 和 `Result<User>` 都是 `Result`，类型参数会被擦掉。所以不能 `new T()`，也不能用 `instanceof Result<Ticket>` 把它们区分开。

编译期仍然能检查 `getData()` 的类型，调用方不用再强转。

## 6. Checked / Runtime Exception

受检异常是 `Exception` 中除去 `RuntimeException` 的部分。方法必须接住它，或者在签名上写 `throws`。`RuntimeException` 没有这个强制要求。

`BusinessException` 继承 `RuntimeException`。工单不存在、用户名重复时直接抛出，到外层再收成 `Result`。受检异常留给必须处理的外部故障，例如文件或网络中断。

## 7. Stream 中间操作为什么 lazy

`filter`、`map` 只记录步骤，遇到 `collect`、`count` 才真正遍历。多次中间操作可以合成一次遍历，`findFirst` 也可以提前停。

本周的按状态过滤，要等后面的 `collect` 才会执行。分组用一次 `groupingBy(Ticket::getStatus)` 即可。

## 8. ConcurrentHashMap 能否保证同一用户不创建重复工单

不能。它保证的是这个映射并发读写时的线程安全，不保证业务唯一。两个请求可以同时发现“还没有这张工单”，然后都 `put` 成功。

要靠业务唯一键、数据库唯一约束、幂等键和事务。本周创建用户时先查用户名再放入，单线程能抛出 `USERNAME_EXISTS`，并发下仍然需要唯一约束。

## 9. 为什么官方文档和 Issue 不能混成一种来源

官方文档是事实依据，优先级最高。GitHub Issue 是历史案例，不能当成官方结论。混成同一个 `sourceType` 后，就分不清回答来自文档还是来自某一次个案。

`KnowledgeSource` 因此分成 `OFFICIAL_DOC` 和 `GITHUB_ISSUE`，并保留产品、标题、原文地址、许可证和文档版本。

## 10. 为什么业务方法抛异常，而不是直接返回 Result

`findTicket` 只负责规则：`id` 为空抛 `INVALID_PARAMETER`，查不到抛 `TICKET_NOT_FOUND`，查到就返回 `Ticket`。

外层再把异常收成 `Result.fail`。以后用 Spring 的全局异常处理替换这段 `try/catch` 时，业务方法不用改。
