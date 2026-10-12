# Kubernetes Docs

- source URL: https://github.com/kubernetes/website
- 本地路径: `data/raw/kubernetes-docs`
- license: CC-BY-4.0（仓库根目录 `LICENSE` 是 Creative Commons Attribution 4.0）
- commit SHA: `b2e60d8de29742f310420999423fd50f87a6b6b6`
- 上游提交时间: 2026-10-10 06:43:16 +0000
- download date: 2026-10-10

只检出 `content/en/`、`content/zh-cn/` 的 Markdown，以及根目录 `LICENSE`。第 8 周清洗时：

- `content/en/**/*.md`：`product` 用 `kubernetes`，语言是英文官方文档
- `content/zh-cn/**/*.md`：同一产品的中文译文，`source_type` 仍是 `official_doc`
- `source_version` 用上面的 commit SHA
