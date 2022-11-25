package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_audit_user_record.groovy') {
    changeSet(author: 'wanghao', id: '2022-11-02-create-table') {
        createTable(tableName: "devops_ci_audit_user_record", remarks: 'ci 人工卡点用户审核记录表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'ci_pipeline_id', type: 'BIGINT UNSIGNED', remarks: '流水线id') {
                constraints(nullable: false)
            }
            column(name: "audit_record_id", type: "BIGINT UNSIGNED", remarks: "devops_ci_audit_record.id") {
                constraints(nullable: false)
            }
            column(name: 'user_id', type: 'BIGINT UNSIGNED', remarks: '用户Id') {
                constraints(nullable: false)
            }
            column(name: 'status', type: 'VARCHAR(64)', defaultValue: 'not_audit', remarks: '人工审核的结果（待审核、拒绝、通过）') {
                constraints(nullable: false)
            }
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_ci_audit_user_record',
                constraintName: 'uk_1', columnNames: 'audit_record_id,user_id')
    }
    changeSet(author: 'wanghao', id: '2022-11-25-add-index') {
        createIndex(tableName: 'devops_ci_audit_user_record', indexName: 'devops_ci_audit_user_record_n1') {
            column(name: 'ci_pipeline_id')
        }
    }

}