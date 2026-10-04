# DevSupport Agent Platform

企业研发支持与故障排查平台。研发人员提出问题后，系统识别意图，检索官方文档和历史 Issue，生成诊断；问题仍未解决时创建支持工单。

Java 后端负责用户、工单和知识来源等业务数据。检索与诊断由后续的 Python Agent 完成，模型调用由 Go 网关完成。

## 当前进度

第一周完成了纯 Java 实验，代码在 `backend-java/java-basics-lab`。

- 模型：`User`、`Ticket`、`KnowledgeSource`
- 统一返回与错误码：`Result<T>`、`ErrorCode`、`BusinessException`
- 实验：集合、HashMap、Stream、业务错误码
- 流程图：`docs/architecture/week1-business-flow.md`

Spring Boot 服务、Python Agent、Go 网关和 MCP 服务在后续周次实现。

## 运行实验

环境：JDK 21、Maven 3.8。

```bash
cd backend-java/java-basics-lab
mvn -q -DskipTests compile
java -cp target/classes com.example.devsupport.CollectiveLab
```

## 目录

```text
backend-java/java-basics-lab    第一周 Java 实验
docs/architecture               业务流程图
docs/experiments                实验记录
docs/interview                  面试题复盘
agent-python                    Python Agent，后续实现
llm-gateway-go                  Go 模型网关，后续实现
mcp-servers                     MCP 服务，后续实现
data                            原始数据与处理后的数据，后续采集
```

## 数据来源与许可证

```text
Docker documentation:
Source: https://github.com/docker/docs
License: Apache-2.0

Kubernetes documentation:
Source: https://github.com/kubernetes/website
License: CC BY 4.0

Stack Overflow:
Public contributions are licensed under applicable CC BY-SA versions.
See: https://stackoverflow.com/help/licensing
```

GitHub Issue 保留原始链接，不把 Issue 内容当作本仓库原创。仓库提交采集脚本，不把第三方文档和 Issue 正文打包进 Git。
