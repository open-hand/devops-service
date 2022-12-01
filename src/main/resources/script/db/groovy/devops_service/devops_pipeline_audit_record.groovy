package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_pipeline_audit_record.groovy') {
    changeSet(author: 'wanghao', id: '2022-11-22-create-table') {
        createTable(tableName: "devops_pipeline_audit_record", remarks: '人工卡点审核记录表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'pipeline_id', type: 'BIGINT UNSIGNED', remarks: '流水线id') {
                constraints(nullable: false)
            }
            column(name: 'pipeline_record_id', type: 'BIGINT UNSIGNED', remarks: '关联流水线记录Id,devops_pipeline_record.id') {
                constraints(nullable: false)
            }
            column(name: "job_record_id", type: "BIGINT UNSIGNED", remarks: "devops_pipeline_job_record.id") {
                constraints(nullable: false)
            }
            column(name: 'countersigned', type: 'TINYINT UNSIGNED', defaultValue: 0, remarks: '是否会签 1是会签,0 是或签') {
                constraints(nullable: false)
            }
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_pipeline_audit_record',
                constraintName: 'devops_pipeline_audit_record_u1', columnNames: 'job_record_id')
    }
    changeSet(author: 'wanghao', id: '2022-11-25-add-index') {
        createIndex(tableName: 'devops_pipeline_audit_record', indexName: 'devops_pipeline_audit_record_n1') {
            column(name: 'pipeline_id')
        }
        createIndex(tableName: 'devops_pipeline_audit_record', indexName: 'devops_pipeline_audit_record_n2') {
            column(name: 'pipeline_record_id')
        }
    }

}