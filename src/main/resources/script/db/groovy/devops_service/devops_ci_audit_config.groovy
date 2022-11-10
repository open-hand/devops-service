package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_audit_config.groovy') {
    changeSet(author: 'wanghao', id: '2022-11-02-create-table') {
        createTable(tableName: "devops_ci_audit_config", remarks: 'ci 人工卡点配置表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'ci_pipeline_id', type: 'BIGINT UNSIGNED', remarks: '流水线id') {
                constraints(nullable: false)
            }
            column(name: 'is_countersigned', type: 'TINYINT UNSIGNED', defaultValue: 0, remarks: '是否会签 1是会签,0 是或签') {
                constraints(nullable: false)
            }
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
    changeSet(author: 'wanghao', id: '2022-11-10-add-index') {
        createIndex(tableName: 'devops_ci_audit_config', indexName: 'devops_ci_audit_config_n1') {
            column(name: 'ci_pipeline_id')
        }
    }

}