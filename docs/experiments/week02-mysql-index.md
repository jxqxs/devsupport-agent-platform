# Week 2 MySQL 索引实验

表是 `devsupport_lab.support_tickets`，10 万条虚拟工单。进入库：

```bash
sudo docker exec -it mysql8 mysql -uroot -p123456 devsupport_lab
```

基准 SQL：

```sql
EXPLAIN SELECT *
FROM support_tickets
WHERE status='OPEN'
ORDER BY created_at DESC
LIMIT 20;
```

记录 `type`、`key`、`rows`、`Extra`。

## 只有主键

`id` 是主键，名字 `PRIMARY`。`status` 和 `created_at` 上还没有二级索引。

```text
type = ALL
key  = NULL
rows = 99775
Extra = Using where; Using filesort
```

`ALL` 表示整表扫描。`key` 是 `NULL`，这次没有走二级索引。扫完再用 `Using filesort` 按 `created_at` 排序，然后取 20 行。

## INDEX(status)

```sql
CREATE INDEX idx_status ON support_tickets(status);
```

```text
type = ref
key  = idx_status
rows = 49887
Extra = Using filesort
```

`ref` 表示用 `idx_status` 按 `status='OPEN'` 定位。估计要看 49887 行，大约是全表的一半。索引里没有 `created_at`，`ORDER BY created_at DESC` 仍然要 `Using filesort`。

## INDEX(status, created_at)

```sql
CREATE INDEX idx_status_createAt ON support_tickets(status, created_at);
```

这次表上同时有 `idx_status` 和 `idx_status_createAt`。优化器选了联合索引。

```text
type = ref
key  = idx_status_createAt
rows = 49887
Extra = Backward index scan
```

联合索引先按 `status` 分组，同一状态下 `created_at` 已经排好。`Backward index scan` 表示倒着读这个索引就能满足 `DESC`，不再单独 `filesort`。`rows` 仍是 49887，这是 `status='OPEN'` 的估计行数，`LIMIT 20` 不会把估计值改成 20。
