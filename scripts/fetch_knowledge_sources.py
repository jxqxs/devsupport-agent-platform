#!/usr/bin/env python3
"""下载后续检索要用的 GitHub Issue 和 Stack Overflow 问题。

官方文档用 git clone，不在这个脚本里。
Issue 去掉带 pull_request 的记录。字段对齐工程手册。
"""

import json
import subprocess
import time
import urllib.parse
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ISSUE_DIR = ROOT / "data" / "raw" / "github-issues"
SO_DIR = ROOT / "data" / "raw" / "stackoverflow"

# 后面的示例问题是 Docker Compose 网络和 Kubernetes。每个仓库取最近更新的 Issue。
REPOS = (
    ("docker/compose", "docker"),
    ("moby/moby", "docker"),
    ("kubernetes/kubernetes", "kubernetes"),
)
ISSUE_PAGES = 5
PER_PAGE = 20

STACK_TAGS = ("docker", "kubernetes")
STACK_PAGESIZE = 30


def get_json(url, headers=None):
    command = [
        "curl",
        "--fail",
        "--silent",
        "--show-error",
        "--location",
        "--http1.1",
        "--retry",
        "5",
        "--retry-all-errors",
        "--retry-delay",
        "2",
        "--max-time",
        "90",
        "-A",
        "devsupport-lab",
    ]
    for key, value in (headers or {}).items():
        command.extend(["-H", f"{key}: {value}"])
    command.append(url)
    last_error = None
    for attempt in range(3):
        try:
            completed = subprocess.run(command, check=True, capture_output=True)
            return json.loads(completed.stdout.decode("utf-8"))
        except Exception as error:
            last_error = error
            time.sleep(2 * (attempt + 1))
    raise last_error


def fetch_issues():
    ISSUE_DIR.mkdir(parents=True, exist_ok=True)
    headers = {
        "Accept": "application/vnd.github+json",
        "User-Agent": "devsupport-lab",
        "X-GitHub-Api-Version": "2022-11-28",
    }
    for repo, product in REPOS:
        records = []
        for page in range(1, ISSUE_PAGES + 1):
            query = urllib.parse.urlencode(
                {
                    "q": f"repo:{repo} is:issue",
                    "sort": "updated",
                    "order": "desc",
                    "per_page": PER_PAGE,
                    "page": page,
                }
            )
            payload = get_json(
                f"https://api.github.com/search/issues?{query}",
                headers,
            )
            for issue in payload.get("items", []):
                if issue.get("pull_request"):
                    continue
                records.append(
                    {
                        "repo": repo,
                        "product": product,
                        "number": issue["number"],
                        "title": issue.get("title"),
                        "body": issue.get("body"),
                        "state": issue.get("state"),
                        "labels": [label["name"] for label in issue.get("labels") or []],
                        "html_url": issue.get("html_url"),
                        "created_at": issue.get("created_at"),
                        "updated_at": issue.get("updated_at"),
                        "closed_at": issue.get("closed_at"),
                    }
                )
            time.sleep(2)
        slug = repo.replace("/", "__")
        path = ISSUE_DIR / f"{slug}.jsonl"
        with path.open("w", encoding="utf-8") as handle:
            for record in records:
                line = json.dumps(record, ensure_ascii=False)
                line = line.replace("\u2028", "\\u2028").replace("\u2029", "\\u2029")
                handle.write(line + "\n")
        print(f"{repo}: {len(records)} issues -> {path.relative_to(ROOT)}")


def fetch_stackoverflow():
    SO_DIR.mkdir(parents=True, exist_ok=True)
    for tag in STACK_TAGS:
        query = urllib.parse.urlencode(
            {
                "order": "desc",
                "sort": "votes",
                "tagged": tag,
                "site": "stackoverflow",
                "pagesize": STACK_PAGESIZE,
                "filter": "withbody",
            }
        )
        payload = get_json(f"https://api.stackexchange.com/2.3/questions?{query}")
        path = SO_DIR / f"{tag}.jsonl"
        count = 0
        with path.open("w", encoding="utf-8") as handle:
            for question in payload.get("items", []):
                record = {
                    "product": tag,
                    "question_id": question.get("question_id"),
                    "title": question.get("title"),
                    "body": question.get("body"),
                    "tags": question.get("tags") or [],
                    "score": question.get("score"),
                    "link": question.get("link"),
                    "license": "CC-BY-SA-4.0",
                }
                line = json.dumps(record, ensure_ascii=False)
                line = line.replace("\u2028", "\\u2028").replace("\u2029", "\\u2029")
                handle.write(line + "\n")
                count += 1
        print(f"stackoverflow/{tag}: {count} questions -> {path.relative_to(ROOT)}")
        time.sleep(1)


if __name__ == "__main__":
    fetch_issues()
    fetch_stackoverflow()
