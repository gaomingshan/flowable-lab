CREATE TABLE IF NOT EXISTS wf_definition (
    id VARCHAR(64) PRIMARY KEY,
    definition_key VARCHAR(128) NOT NULL,
    definition_name VARCHAR(256) NOT NULL,
    category_code VARCHAR(128) NOT NULL,
    description VARCHAR(1024) NULL,
    status VARCHAR(32) NOT NULL,
    latest_draft_version INT NOT NULL,
    latest_released_version INT NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_wf_definition_key (definition_key)
);

CREATE TABLE IF NOT EXISTS wf_definition_version (
    id VARCHAR(64) PRIMARY KEY,
    definition_id VARCHAR(64) NOT NULL,
    version_no INT NOT NULL,
    change_note VARCHAR(512) NULL,
    status VARCHAR(32) NOT NULL,
    graph_json LONGTEXT NOT NULL,
    release_comment VARCHAR(512) NULL,
    deployment_id VARCHAR(128) NULL,
    process_definition_id VARCHAR(128) NULL,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    released_at DATETIME NULL,
    UNIQUE KEY uk_wf_definition_version (definition_id, version_no),
    KEY idx_wf_definition_version_definition (definition_id)
);

CREATE TABLE IF NOT EXISTS wf_node (
    id VARCHAR(64) PRIMARY KEY,
    version_id VARCHAR(64) NOT NULL,
    node_key VARCHAR(128) NOT NULL,
    node_name VARCHAR(256) NULL,
    node_type VARCHAR(64) NOT NULL,
    incoming_count INT NOT NULL,
    outgoing_count INT NOT NULL,
    sort_order INT NOT NULL,
    KEY idx_wf_node_version (version_id)
);

CREATE TABLE IF NOT EXISTS wf_edge (
    id VARCHAR(64) PRIMARY KEY,
    version_id VARCHAR(64) NOT NULL,
    edge_key VARCHAR(128) NOT NULL,
    source_node_key VARCHAR(128) NOT NULL,
    target_node_key VARCHAR(128) NOT NULL,
    edge_type VARCHAR(64) NOT NULL,
    condition_expression VARCHAR(1024) NULL,
    is_default TINYINT(1) NOT NULL,
    sort_order INT NOT NULL,
    KEY idx_wf_edge_version (version_id)
);
