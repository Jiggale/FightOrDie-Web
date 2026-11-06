# 《燃薪者》共享协作空间

这里用于 Codex、CodeBuddy 和项目所有者之间的异步协作。GitHub Issue 用于讨论具体任务，Pull Request 用于审查实现，仓库文档用于保存最终事实。

## 文件用途

| 文件 | 用途 |
|---|---|
| `PROJECT_BRIEF.md` | 项目的稳定目标、核心体验和范围 |
| `DECISIONS.md` | 已确认、暂定或拒绝的重要决定 |
| `ACTIVE_TASK.md` | 当前唯一可以实施的任务 |
| `OPEN_QUESTIONS.md` | 尚未解决、不得擅自假设的问题 |
| `HANDOFF.md` | CodeBuddy 每次实现后的交付报告 |
| `REVIEW_LOG.md` | Codex 对实现和设计的审查记录 |
| `TASK_ARCHIVE.md` | 已完成或被否决任务的索引 |

## 标准工作流

1. 用户在 GitHub Issue 或对话中提出目标。
2. Codex 将目标整理为完整任务包，并更新 `ACTIVE_TASK.md`。
3. 用户确认任务范围。
4. CodeBuddy 在独立分支实施，只修改任务涉及的文件。
5. CodeBuddy 更新 `HANDOFF.md`，提交 Pull Request。
6. Codex 审查代码、测试和体验，将结论写入 `REVIEW_LOG.md`。
7. 用户决定接受、返工或放弃。
8. 合并后把任务移入 `TASK_ARCHIVE.md`，并同步更新 `DECISIONS.md`。

## 交给 CodeBuddy 的启动指令

```text
请先完整阅读仓库根目录的 AGENTS.md，以及 collab/PROJECT_BRIEF.md、
collab/DECISIONS.md、collab/ACTIVE_TASK.md、collab/OPEN_QUESTIONS.md。

你只实施 ACTIVE_TASK.md 中“状态”为“已批准”的内容。
不要自行增加玩法、系统、页面、依赖或技术方案。
如有歧义，先提出问题，不要假设。
完成后使用独立分支提交 Pull Request，并填写 collab/HANDOFF.md。
```

## 交给 Codex 的审查指令

```text
请以 collab/ACTIVE_TASK.md 的验收标准为唯一完成标准，审查对应分支或 PR。
检查范围蔓延、设计偏离、代码质量、测试证据和玩家体验。
只输出明确的通过项、阻塞项和建议项，并更新 collab/REVIEW_LOG.md。
```

## 重要原则

不要把完整聊天记录直接交给实施 agent。聊天中混有已否定方案、临时假设和互相冲突的灵感。CodeBuddy 应读取整理后的任务包，而不是猜测哪些内容已经生效。
