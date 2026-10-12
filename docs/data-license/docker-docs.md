# Docker Docs

- source URL: https://github.com/docker/docs
- 本地路径: `data/raw/docker-docs`
- license: Apache-2.0（仓库根目录 `LICENSE`）
- commit SHA: `18bbfeeb249da011f359d558dba84c4b6dc3a335`
- 上游提交时间: 2026-10-09 17:05:49 +0200
- download date: 2026-10-10

只检出 `content/` 下的 Markdown，以及根目录 `LICENSE`。图片和其他语言页面没有下载。第 8 周清洗时从 `content/**/*.md` 和 `content/**/*.mdx` 读正文，`source_type` 用 `official_doc`，`product` 用 `docker`，`source_version` 用上面的 commit SHA。
