# Week 2 JVM 实验

类都在 `com.example.devsupport.juc`。编译：

```bash
cd backend-java/java-basics-lab
mvn -q -DskipTests compile
```

## 堆溢出

`OomDemo` 把 1MB 的 `byte[]` 放进 `List`，引用一直留着，垃圾回收收不走。用 32MB 的堆启动：

```bash
java -Xms32m -Xmx32m -cp target/classes com.example.devsupport.juc.OomDemo
```

```text
创建了 1MB，总共 1MB
...
创建了 1MB，总共 14MB
内存不足
```

堆上限是 32MB，到不了 32 块。`ArrayList`、字符串和 JVM 自己还要占地方。堆满时，打印那句字符串拼接也会分配内存，所以 `OutOfMemoryError: Java heap space` 先出现在打印上。`catch` 里先 `holder.clear()`，数组不再被引用，后面才能打出「内存不足」。

## 栈溢出

`StackOverflowDemo` 里 `test1()` 只调用自己。每次调用都在当前线程的栈上压入一帧。`main` 接住 `StackOverflowError` 再打印，这时递归帧已经退出，打印不会再把栈撑满。

```bash
java -cp target/classes com.example.devsupport.juc.StackOverflowDemo
```

```text
栈溢出, 总共调用了22239次
```

次数由默认栈大小决定，每次运行会有出入。计数器是 `static`，所有帧共用同一个 `total`。

## Arthas

`ArthasDemo` 在 `while (true)` 里把 `total` 加 1、打印，然后 `Thread.sleep(1000)`，进程不会自己退出。另开一个终端：

```bash
curl -O https://arthas.aliyun.com/arthas-boot.jar
java -jar arthas-boot.jar
```

挂上 `ArthasDemo` 之后练了四个命令。

`dashboard` 里堆大约用了 19MB，最大 932MB，占用约 2%。这次没有加 `-Xmx32m`，所以不会像 `OomDemo` 那样撑满。年轻代回收了 4 次，一共大约 22ms；老年代还是 0 次。`main` 的状态是 `TIMED_WAITING`，CPU 接近 0，因为它大部分时间在 `sleep`。

`thread` 看到大约 17 条线程，`BLOCKED` 是 0，没有线程卡在锁上。`main` 的 ID 是 1。

`jad com.example.devsupport.juc.ArthasDemo` 反编译的是 `target/classes/` 里已经加载的 class。能看到 `while (true)`、`++total` 和 `Thread.sleep(1000L)`。

`trace com.example.devsupport.juc.ArthasDemo test1` 已经挂上，`method count: 1`。`trace` 等方法返回才打出耗时，`test1` 的死循环不返回，所以没有耗时数字。
