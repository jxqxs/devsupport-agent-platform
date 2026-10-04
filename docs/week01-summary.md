# Week 1 总结

## 本周做了什么

建好了 `devsupport-agent-platform` 仓库，并完成第一周的纯 Java 实验，代码在 `backend-java/java-basics-lab`。

- 模型：`User`、`Ticket`、`KnowledgeSource`
- 统一返回和错误码：`Result<T>`、`ErrorCode`、`BusinessException`
- 跑通了 `ArrayList` 与 `LinkedList` 随机访问、`HashSet` 的 `equals` / `hashCode`、可变对象当 `HashMap` key、按工单状态过滤和分组
- 四个错误码都走到了「抛 `BusinessException`，外层收成 `Result`」
- 业务流程写在 `docs/architecture/week1-business-flow.md`

## 遇到了什么

1. 公共类文件名前面多了一个空格，编译器要求类必须放在同名文件里。
2. 编辑器生成了一份带 `Unresolved compilation problems` 的 `.class`，它比源文件新，`mvn compile` 没有重新编译，运行时直接抛出这个错误。
3. `Result.fail` 只接收 `ErrorCode`，一开始把 `findTicket` 的返回值传了进去。`findTicket` 成功时返回的是 `Ticket`。
4. `USERNAME_EXISTS` 起初写在工单 `id` 重复上，和「创建用户时用户名已存在」对不上。

## 怎么排查

文件名问题看报错里的类名和实际文件名，去掉空格后通过。

运行时的 `Unresolved compilation problems` 用 `javap` 看了 `.class`，确认方法体就是直接抛出这段错误。源文件时间比 class 旧，所以改成 `mvn clean -DskipTests compile`，清掉 `target` 再编译。

`fail` 的参数错误看方法签名，失败分支改为 `Result.fail(exception.getErrorCode())`。用户名那条改成 `createUser`，用用户名当键，重复创建 `alice` 时得到 `USERNAME_EXISTS`。

## 为什么这样设计

工单用来留下没解决的问题，后面还要改状态、加评论。知识来源只存元数据，不把文档正文放进这个类，答案才能回到类型、产品、地址和许可证。官方文档是事实依据，Issue 是历史案例，所以 `sourceType` 分成 `OFFICIAL_DOC` 和 `GITHUB_ISSUE`。

业务方法只抛带错误码的异常，外层统一变成 `Result`。以后 Spring Boot 可以用全局异常处理替换每个方法里的 `try/catch`。

## 还有什么问题

仓库里目前只有初始化那一次 commit，实验代码和文档还没提交。每周标准要求至少 5 个 commit。

第六天要求不看旧代码重写 `Result` 和 `Ticket`，并做两道 LeetCode Medium。这两项还没有按这个要求做。

`createUser` 是先查再放。单线程能挡住重复用户名，并发时仍然需要数据库唯一约束。

## 如果重做会怎么改

错误码一开始就按业务对象分开写：工单按 `id` 查，知识来源按 `id` 查，用户按用户名创建。不要先借工单的 `id` 模拟 `USERNAME_EXISTS`。

集合实验和错误码实验分成两个入口，避免 `main` 里注释来回切换。随机访问的列表不用一开始放到 100 万，10 万条已经能看出 `LinkedList` 的差距。

编译固定用 `mvn clean -DskipTests compile`，避免编辑器留下的 class 被跳过。
