## Flowable Lab Product Architecture Design

### 1. Goal

This document defines the target product architecture for a workflow platform built on top of Flowable while keeping Flowable strictly limited to the engine layer.

The platform goals are:

- Keep frontend modeling focused on graph interaction only: nodes, edges, layout, and canvas state.
- Move business configuration out of the graph payload and into dynamic platform APIs.
- Maintain a semantic platform workflow model that is independent from Flowable internals.
- Use Flowable as the runtime engine only after publish-time compilation.
- Build platform-owned runtime query, operations, and governance capabilities instead of exposing Flowable directly.

### 2. Core Principles

#### 2.1 Design-time and runtime are separated

The platform model is the source of truth at design time.

Flowable BPMN is a compiled deployment artifact, not the primary design object.

#### 2.2 Frontend graph is not the business model

The frontend only stores graph topology and layout state.

Node business semantics are configured through platform APIs and dynamic schemas.

#### 2.3 Flowable is a bounded engine dependency

Flowable is responsible for:

- process definition execution
- user tasks
- gateways and multi-instance execution
- timers
- runtime state
- history

Flowable is not responsible for:

- platform workflow semantics
- dynamic configuration UI
- business document model
- attachment model
- opinion model
- query workbench
- operations governance

#### 2.4 Platform owns product semantics

Platform semantics include:

- approval node semantics
- countersign semantics
- return policy
- candidate resolution policy
- reminder policy
- action policy
- process operations policy

### 3. High-level Layering

The platform should be split into six logical layers.

#### 3.1 Graph Layer

Responsible for:

- node and edge rendering
- layout persistence
- canvas interaction
- selection, drag, zoom, and alignment

Graph payload example:

```json
{
  "graph": {
    "nodes": [
      { "id": "n_start", "type": "start", "x": 120, "y": 160 },
      { "id": "n_approve", "type": "approval", "x": 380, "y": 160 }
    ],
    "edges": [
      { "id": "e_1", "source": "n_start", "target": "n_approve" }
    ]
  }
}
```

This layer must not embed full business configuration.

#### 3.2 Semantic Model Layer

Responsible for:

- workflow definition
- node semantic type system
- node configuration schema
- validation rules
- business capabilities

This is the platform workflow model, independent from Flowable naming and BPMN implementation details.

#### 3.3 Release Layer

Responsible for:

- validation
- compilation
- release approval
- deployment orchestration
- artifact persistence
- engine binding

This layer transforms the platform model into Flowable-consumable artifacts.

#### 3.4 Engine Layer

Responsible for:

- deployment to Flowable
- process instance start
- task lifecycle integration
- runtime commands
- history queries for technical drill-down

This layer should be wrapped by platform services and never exposed directly to frontend clients.

#### 3.5 Runtime Projection Layer

Responsible for:

- todo list
- done list
- initiated process list
- process center
- advanced search
- reporting projections
- timeline summary

This is a platform-owned read model and must not depend on raw Flowable table structures as public contract.

#### 3.6 Operations Layer

Responsible for:

- process operations and governance
- manual intervention
- audit logging
- diagnostics
- state repair
- reassignment
- migration and rollback support

This is a platform-level capability, not an engine console.

### 4. Design-time Object Model

#### 4.1 Workflow Definition

Represents the logical identity of a platform workflow.

Suggested fields:

- id
- key
- name
- category
- tenantId
- status
- latestVersion
- createdBy
- updatedBy
- createdAt
- updatedAt

#### 4.2 Workflow Definition Version

Represents an immutable design snapshot.

Suggested fields:

- id
- definitionId
- version
- graphJson
- semanticSnapshotJson
- validationReportJson
- status
- changeNote
- createdBy
- createdAt

#### 4.3 Node Configuration

Node configuration should be managed separately from the graph payload.

Suggested fields:

- id
- definitionVersionId
- nodeId
- nodeType
- configSchemaKey
- configJson
- capabilityFlagsJson
- updatedBy
- updatedAt

This allows dynamic UI composition by node type and schema.

### 5. Dynamic Configuration API Strategy

The platform should expose node-centric APIs rather than whole-document static contracts.

Suggested endpoints:

- `GET /workflow-definitions/{id}/nodes/{nodeId}/schema`
- `GET /workflow-definitions/{id}/nodes/{nodeId}/config`
- `PUT /workflow-definitions/{id}/nodes/{nodeId}/config`
- `POST /workflow-definitions/{id}/validate`
- `POST /workflow-definitions/{id}/release`

This supports dynamic extension scenarios such as:

- reminder policy
- timeout policy
- return policy
- countersign rule
- candidate strategy
- attachment requirement
- opinion requirement

Example dynamic configuration payload:

```json
{
  "schemaKey": "approval-node/v2",
  "config": {
    "candidatePolicy": {
      "type": "role",
      "value": "dept_manager"
    },
    "returnPolicy": "previous-node",
    "reminderPolicy": {
      "enabled": true,
      "firstAfterMinutes": 30,
      "repeatEveryMinutes": 60,
      "channels": ["inbox", "sms"]
    }
  }
}
```

### 6. Compilation and Release Pipeline

Release must be explicit and pipeline-based.

Suggested pipeline:

1. Graph validation
2. Semantic validation
3. Business rule validation
4. Intermediate model construction
5. BPMN generation
6. Flowable parse validation
7. Deployment to Flowable
8. Release artifact persistence
9. Engine binding persistence

#### 6.1 Release Artifact

Suggested fields:

- releaseId
- definitionId
- definitionVersionId
- releaseVersion
- compiledBpmnXml
- compiledDiagramSvg
- validationReportJson
- engineType
- deploymentId
- processDefinitionId
- processDefinitionKey
- processDefinitionVersion
- releasedBy
- releasedAt

### 7. Relationship Between Platform Model and Flowable

The platform model is the product-facing source of truth.

Flowable artifacts are generated from platform releases.

The platform model should be mostly a subset of Flowable execution semantics, but it can include additional business semantics such as:

- reminder policy
- return policy
- attachment requirement
- opinion requirement
- business action rule
- candidate resolution strategy
- workbench display metadata

These semantics may compile into:

- BPMN constructs
- Flowable extension properties
- listeners
- external platform services
- scheduled jobs

### 8. Runtime Data Strategy

#### 8.1 Principle

Flowable is the execution truth source.

The platform must maintain its own runtime projection model for business query and workbench scenarios.

The platform should not clone all Flowable tables.

Instead, it should maintain targeted projections and business extension records.

#### 8.2 Why platform projections are necessary

Platform queries usually require business semantics beyond engine semantics, for example:

- todo filtering by business type, title, department, urgency
- search by document text, attachment name, opinion content
- operation statistics by tenant, workflow, node, and time range
- candidate resolution snapshots
- return and reassignment history

These are not good fits for direct Flowable table queries.

#### 8.3 Suggested runtime projection tables

##### Workflow Instance Projection

`wf_instance`

- id
- tenant_id
- definition_id
- definition_version_id
- release_id
- engine_process_instance_id
- engine_process_definition_id
- business_key
- title
- starter_id
- starter_name
- starter_dept_id
- status
- current_node_id
- current_node_name
- started_at
- ended_at
- search_text

##### Workflow Task Projection

`wf_task`

- id
- instance_id
- node_id
- node_name
- engine_task_id
- assignee_id
- assignee_name
- candidate_snapshot_json
- status
- created_at
- claimed_at
- completed_at
- action_result
- urgency

##### Workflow Action Record

`wf_action`

- id
- instance_id
- task_id
- node_id
- operator_id
- action_type
- opinion_text
- attachments_json
- created_at

##### Workflow Event Record

`wf_event`

- id
- instance_id
- task_id
- event_type
- payload_json
- created_at

#### 8.4 Synchronization strategy

Recommended combined strategy:

- synchronous writes for core platform records
- event-driven projection updates for workbench and search
- periodic reconciliation for recovery and consistency checking

### 9. Query Strategy

#### 9.1 Queries that can remain engine-facing

- start process
- complete task
- current runtime state drill-down
- detailed technical history analysis

#### 9.2 Queries that should use platform read models

- my todo
- my done
- initiated workflows
- advanced search
- process center
- dashboard statistics
- SLA monitoring
- reporting

### 10. Process Operations Architecture

Process operations are platform responsibilities.

They must not be exposed as raw Flowable engine APIs.

#### 10.1 Operations capabilities

Suggested capabilities:

- suspend instance
- activate instance
- terminate instance
- reassign task
- transfer task
- add signers
- remove signers
- return to previous node
- return to starter
- jump to target node
- repair candidate assignment
- retry failed step
- migrate running instance

#### 10.2 Operations command model

Suggested payload:

```json
{
  "actionType": "RETURN_TO_NODE",
  "instanceId": "wf_inst_1001",
  "taskId": "wf_task_2001",
  "targetNodeId": "deptApprove",
  "reason": "original assignee left the organization",
  "operatorId": "admin_01",
  "options": {
    "keepHistory": true,
    "notifyRelatedUsers": true,
    "restoreOriginalAssignee": false
  }
}
```

#### 10.3 Operations processing flow

1. permission check
2. instance state validation
3. operations policy validation
4. translation into engine command(s)
5. Flowable execution
6. operations audit log persistence
7. runtime projection refresh
8. event and notification dispatch

#### 10.4 Operations audit log

Suggested table:

`wf_ops_log`

- id
- tenant_id
- instance_id
- task_id
- action_type
- operator_id
- operator_name
- reason
- before_snapshot_json
- after_snapshot_json
- engine_command_ref
- result
- created_at

### 11. Recommended Service Boundaries

Suggested platform services:

- `WorkflowDefinitionService`
- `NodeConfigService`
- `WorkflowValidationService`
- `WorkflowReleaseService`
- `WorkflowCompilerService`
- `FlowableAdapterService`
- `WorkflowRuntimeFacade`
- `WorkflowProjectionService`
- `WorkflowSearchService`
- `WorkflowOperationsService`
- `WorkflowAuditService`
- `WorkflowEventBridge`

### 12. Frontend Integration Guidelines

Frontend should be built around these principles:

- graph editor only handles visual topology
- configuration panels are schema-driven
- node capability loading is dynamic
- release actions call platform APIs only
- frontend never depends on Flowable identifiers as primary UI contract

Suggested frontend dynamic loading mechanisms:

- node definition registry
- schema-driven form rendering
- node capability metadata API
- permission-based panel control

### 13. Risks and Controls

#### 13.1 Risk: custom DSL grows into an uncontrolled second BPMN

Control:

- keep node type system small
- version schemas explicitly
- standardize return and countersign policies
- avoid exposing unlimited free-form behaviors

#### 13.2 Risk: release pipeline becomes opaque

Control:

- persist validation reports
- persist compiled artifacts
- persist engine binding metadata
- expose release diagnostics

#### 13.3 Risk: runtime projections drift from engine truth

Control:

- combine synchronous write, async projection, and reconciliation
- monitor projection lag and repair jobs

#### 13.4 Risk: operations become unsafe

Control:

- command-based operations API
- strong audit logging
- permission segmentation
- pre-check and dry-run support where feasible

### 14. Final Recommendation

The recommended architecture is:

- a platform-owned semantic workflow model
- a graph-only frontend editor
- dynamic node configuration APIs
- explicit release-time compilation into Flowable artifacts
- Flowable limited to engine responsibilities
- platform-owned runtime projections for query and search
- platform-owned process operations and governance

This architecture preserves frontend flexibility, supports platform semantic evolution, and keeps Flowable in a healthy bounded role as the runtime engine.
