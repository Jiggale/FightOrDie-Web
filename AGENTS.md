# 项目协作规则

本仓库采用“用户决策、CodeBuddy 实施、Codex 审查”的协作模式。

## 所有 agent 开始工作前必须阅读

1. `collab/PROJECT_BRIEF.md`
2. `collab/DECISIONS.md`
3. `collab/ACTIVE_TASK.md`
4. `collab/OPEN_QUESTIONS.md`

## 权限与职责

- 用户拥有最终决策权与合并权。
- CodeBuddy 只实现 `ACTIVE_TASK.md` 中已批准的范围。
- Codex 负责细化任务、审查实现、检查测试和识别范围蔓延。
- 文档和实现冲突时，以 `collab/DECISIONS.md` 中最新且状态为“已确认”的条目为准。

## 强制规则

- 不得把 agent 自己的建议直接升级为已确认设计。
- 不得自行新增游戏系统、重构范围或技术栈。
- 不得在未写明“明确不做”的情况下开始实现。
- 同一时间只允许一个实施 agent 修改代码。
- CodeBuddy 必须使用独立分支并通过 Pull Request 交付，不得直接推送主分支。
- 每次交付必须更新 `collab/HANDOFF.md`，写明改动、测试证据、已知问题和未完成事项。
- Codex 的审查记录写入 `collab/REVIEW_LOG.md`。
- 只有用户明确接受后才可合并。
- 不得提交密钥、账号、数据库密码、JWT 密钥或其他敏感信息。

## 发生不确定时

不要猜测并继续实现。把问题写入 `collab/OPEN_QUESTIONS.md`，等待用户或 Codex 回答。
