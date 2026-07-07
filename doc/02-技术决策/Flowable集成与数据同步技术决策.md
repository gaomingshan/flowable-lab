## Flowable 集成与数据同步技术决策

### 1. 文档目标

本文档用于明确第一迭代中平台与 Flowable 的集成方式，以及平台主数据、逻辑骨架、外挂配置、基础读模型之间的数据同步策略。

### 2. Flowable 集成原则

第一迭代的 Flowable 集成必须服从以下边界：

1. Flowable 只承担执行引擎职责
2. 平台主导定义、版本、发布、启动和基础查询
3. 平台不直接把前端流程图交给 Flowable
4. 平台先完成自己的模型编译，再交由 Flowable 部署

### 3. 第一迭代 Flowable 集成方式

### 3.1 集成方式选择

建议：

- 采用嵌入式集成 Flowable Engine
- 平台后端直接调用 Flowable Java API

不建议：

- 第一迭代通过 Flowable REST 作为主链路

原因：

- Java API 集成更直接
- 更容易把平台模型和 Flowable 模型转换逻辑放在同一服务层
- 发布、启动、查询、事务处理路径更清晰

### 3.2 发布边界

建议发布边界如下：

1. 平台读取定义版本
2. 平台读取 `graph_json`
3. 平台读取 `wf_node`、`wf_edge`
4. 平台读取节点候选人配置和边条件配置
5. 平台编译为 BPMN XML
6. 平台调用 Flowable 部署
7. 平台保存 `wf_release` 和 `wf_engine_binding`

### 4. 平台模型到 Flowable 模型的转换决策

### 4.1 转换输入

转换输入应包括：

- `wf_definition`
- `wf_definition_version.graph_json`
- `wf_node`
- `wf_edge`
- `wf_node_candidate_config`
- `wf_edge_condition_config`

### 4.2 转换原则

建议：

- `graph_json` 作为完整结构输入
- `wf_node`、`wf_edge` 作为逻辑校验和结构化辅助输入
- 节点和边外挂配置在转换阶段按 `node_key` / `edge_key` 挂接

### 4.3 Flowable 节点映射建议

第一期建议映射：

- 开始节点 -> `startEvent`
- 结束节点 -> `endEvent`
- 用户任务节点 -> `userTask`
- 排他分支节点 -> `exclusiveGateway`
- 并行分支节点 -> `parallelGateway`
- 并行汇聚节点 -> `parallelGateway`

### 4.4 候选人配置映射建议

第一期账号模式映射：

- `policyMode = single` -> `assignee`
- `policyMode = candidate` -> `candidateUsers`

候选人明细从 `policy_payload_json` 中读取。

### 4.5 边条件映射建议

边条件配置映射到：

- `conditionExpression`
- 默认分支属性

### 5. 数据同步策略

### 5.1 图保存同步策略

建议：

- 每次保存 `graph_json` 后，同步刷新 `wf_node`、`wf_edge`

原因：

- 避免后端每次逻辑处理都重新解析 JSON
- 结构化校验更清晰

### 5.2 外挂配置同步策略

建议：

- 节点候选人配置与边条件配置独立保存
- 不回写进 `graph_json`

原因：

- 保持图主存储纯净
- 配置治理和查询路径更清晰

### 5.3 发布同步策略

建议：

- 发布成功后同步写入 `wf_launchable_projection`

原因：

- 让启动页读取稳定
- 不直接暴露 Flowable 流程定义列表

### 5.4 启动同步策略

建议：

- 启动成功后同步写入：
  - `wf_instance`
  - `wf_todo_projection`
  - `wf_initiated_projection`

- 任务处理完成后同步写入：
  - `wf_done_projection`

原因：

- 第一迭代先采用同步写入，降低系统复杂度

### 6. 第一迭代不建议采用的同步策略

第一期不建议：

- 主链路依赖消息中间件保证一致性
- 主链路依赖异步投影更新
- 把基础查询完全建立在 Flowable 原生查询上

原因：

- 会增加调试和联调成本
- 不利于快速验证最小闭环

### 7. 第一迭代异常处理建议

### 7.1 图保存异常

建议：

- `graph_json` 保存和 `wf_node/wf_edge` 同步放在同一事务中

### 7.2 发布异常

建议：

- 编译失败不写入引擎绑定
- Flowable 部署失败时保留 `wf_release` 失败状态

### 7.3 启动异常

建议：

- Flowable 实例启动成功后，再写平台实例与投影
- 平台写入失败时要记录补偿日志，避免实例与投影脱节

### 8. 结论

第一迭代 Flowable 集成与数据同步的核心决策可以收敛为：

- 嵌入式集成 Flowable
- 平台编译后再部署
- `graph_json` 主存储
- `wf_node/wf_edge` 同步骨架
- 独立外挂配置表
- 平台同步写基础读模型

这套方案最适合当前第一期原型：主链路简单、边界清晰、便于调试和收敛。
