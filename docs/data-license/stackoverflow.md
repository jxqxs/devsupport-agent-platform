# Stack Overflow

- API: https://api.stackexchange.com/2.3/questions
- 站点: stackoverflow
- license: CC-BY-SA-4.0
- download date: 2026-10-10
- 本地路径: `data/raw/stackoverflow/docker.jsonl`、`data/raw/stackoverflow/kubernetes.jsonl`

这是按投票从高到低取的社区问题样本：Docker 29 条，Kubernetes 30 条。给后面的 community case 和评测用。完整数据包在 https://archive.org/details/stackexchange ，没有整包下载。

每行字段：`product`、`question_id`、`title`、`body`、`tags`、`score`、`link`、`license`。

第 8 周归一化时，`source_type` 用 `stackoverflow`，`source_url` 用 `link`。它只作为社区参考，排在官方文档和 GitHub Issue 后面。
