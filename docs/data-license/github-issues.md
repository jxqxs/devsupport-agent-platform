# GitHub Issues

- API: https://docs.github.com/en/rest/issues/issues
- 查询: `is:issue`，按最近更新排序。带 `pull_request` 的记录已丢掉
- download date: 2026-10-10
- 本地路径: `data/raw/github-issues/`

| 文件 | repo | product |
| --- | --- | --- |
| `docker__compose.jsonl` | docker/compose | docker |
| `moby__moby.jsonl` | moby/moby | docker |
| `kubernetes__kubernetes.jsonl` | kubernetes/kubernetes | kubernetes |

每行字段：`repo`、`product`、`number`、`title`、`body`、`state`、`labels`、`html_url`、`created_at`、`updated_at`、`closed_at`。

第 8 周归一化时，`source_type` 用 `github_issue`，`source_url` 用 `html_url`，`document_id` 用 `repo` 加 `number`。Issue 是历史案例，不是官方规范。
