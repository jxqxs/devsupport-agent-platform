# Week 2 计数实验

三个类都在 `com.example.devsupport.juc`。编译后分别运行：

```bash
cd backend-java/java-basics-lab
mvn -q -DskipTests compile
java -cp target/classes com.example.devsupport.juc.CounterDemo
java -cp target/classes com.example.devsupport.juc.SynchronizedCounterDemo
java -cp target/classes com.example.devsupport.juc.AtomicIntegerDemo
```

## 不安全的 count++

`CounterDemo` 用两条线程各对普通 `int` 执行 `count++` 10 万次。

```text
unsafe count = 129104, expected 200000
```

`count++` 会拆成读、加 1、写回。两条线程可能读到同一个旧值，各写一次，其中一次加 1 就丢了。所以实际值小于 `200000`，而且每次运行的数字不一定相同。

## synchronized

`SynchronizedCounterDemo` 用同一把 `lock` 包住 `count++`。两条线程各加 1 万次。

```text
count = 20000
expected count = 20000
```

进入 `synchronized (lock)` 的线程持有这把锁，另一条线程要等它出来。临界区里的读、加、写不会被拆开，结果等于预期。

## AtomicInteger

`AtomicIntegerDemo` 用两条线程各调用 `incrementAndGet()` 10 万次。

```text
atomic count = 200000, expected 200000
```

`incrementAndGet()` 用 CAS 完成一次「读出、加 1、写回」。写回时如果发现值已经被别的线程改过，就重试。两条线程仍然并行，这一次更新不会丢。
