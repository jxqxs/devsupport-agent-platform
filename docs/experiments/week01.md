# Week 1 实验记录

实验代码在 `backend-java/java-basics-lab` 的 `CollectiveLab`。编译和运行：

```bash
cd backend-java/java-basics-lab
mvn -q clean -DskipTests compile
java -cp target/classes com.example.devsupport.CollectiveLab
```

集合实验和错误码实验当时是分两次跑的。`main` 里现在只调用了 `testException()`。

## ArrayList 与 LinkedList 随机访问

两个列表各放 10 万条 `Ticket`，用同一个 `Random(1)` 生成 1 万个下标，两边都调用 `get(index)`。

```text
ArrayList random access time: 1ms
LinkedList random access time: 936ms
```

`ArrayList` 按下标可以直接定位。`LinkedList` 每次 `get` 都要从头走到那个下标，所以同样的随机下标慢得多。

## HashSet 与 equals / hashCode

`EqualsOnlyKey` 只按 `id` 重写了 `equals`。两个 `id` 都是 `"u1"` 的对象放进 `HashSet`，大小是 `2`。`equals` 为真，但默认 `hashCode` 不同，它们进了不同的格子。

`EqualsHashKey` 的 `equals` 和 `hashCode` 都按 `id`。同样放两个 `"u1"`，大小是 `1`。

## 可变对象作为 HashMap key

用 `EqualsHashKey("t1")` 放入值 `"OPEN"`，然后把这个对象的 `id` 改成 `"t2"`。

```text
map.get(key)                         null
map.get(new EqualsHashKey("t1"))     null
```

对象仍留在当初 `"t1"` 对应的格子里，但它的 `id` 已经是 `"t2"`。用 `"t2"` 查找会走进另一个格子。用新的 `"t1"` 能走进原来的格子，`equals` 却对不上。`entrySet()` 遍历可以看见这条记录，键显示为 `t2`，值仍是 `OPEN`。

## Stream 按状态过滤和分组

三条 `Ticket`：两条 `OPEN`，一条 `CLOSED`。

```text
filter OPEN 的数量: 2
groupingBy(Ticket::getStatus) 的 key: [CLOSED, OPEN]
```

## 业务错误码

业务方法在规则不成立时抛 `BusinessException`，外层接住后用 `Result.fail(exception.getErrorCode())` 返回。

```text
true 1
false TICKET_NOT_FOUND
false INVALID_PARAMETER
true alice
false USERNAME_EXISTS
true 1
false KNOWLEDGE_SOURCE_NOT_FOUND
false INVALID_PARAMETER
```

- 工单 `id` 为 `"1"` 时查到数据。`"999"` 抛 `TICKET_NOT_FOUND`。空白 `id` 抛 `INVALID_PARAMETER`。
- 第一次创建用户名 `alice` 成功。再用另一个用户编号创建同一个用户名，抛 `USERNAME_EXISTS`。
- 知识来源 `id` 为 `"1"` 时查到数据。`"999"` 抛 `KNOWLEDGE_SOURCE_NOT_FOUND`。空白 `id` 抛 `INVALID_PARAMETER`。

## 真实 Bug

### Bug 1

文件名前面多了一个空格，实际是 ` CollectiveLab.java`，公共类名是 `CollectiveLab`。编译器报公共类必须放在自己的文件里。把文件名改成 `CollectiveLab.java` 后通过。

### Bug 2

运行时报 `Unresolved compilation problems: Ticket cannot be resolved to a type`。源码里的 `import` 是对的，但编辑器先写了一份会直接抛出这个错误的 `.class`，而且它比 `.java` 新。`mvn compile` 认为已经编译过，没有覆盖它。改用 `mvn clean -DskipTests compile` 后，程序正常运行。

### Bug 3

`Result.fail(findTicket(store, id))` 报 `fail(ErrorCode)` 不能接收 `Ticket`。`findTicket` 成功时返回工单，失败时抛异常，不能把它的返回值交给 `fail`。失败分支改为 `Result.fail(exception.getErrorCode())`。
