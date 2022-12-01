package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_audit_record.groovy') {
    changeSet(author: 'wanghao', id: '2022-11-02-create-table') {
        createTable(tableName: "devops_ci_audit_record", remarks: 'ci 人工卡点审核记录表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'ci_pipeline_id', type: 'BIGINT UNSIGNED', remarks: '流水线id') {
                constraints(nullable: false)
            }
            column(name: "app_service_id", type: "BIGINT UNSIGNED", remarks: "devops_app_service.id") {
                constraints(nullable: false)
            }
            column(name: "job_record_id", type: "BIGINT UNSIGNED", remarks: "devops_ci_job_record.id") {
                constraints(nullable: false)
            }
            column(name: 'gitlab_pipeline_id', type: 'BIGINT UNSIGNED', remarks: 'gitlabPipelineId') {
                constraints(nullable: false)
            }
            column(name: "job_name", type: "VARCHAR(255)", remarks: "任务名称") {
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
        addUniqueConstraint(tableName: 'devops_ci_audit_record',
                constraintName: 'uk_1', columnNames: 'app_service_id,gitlab_pipeline_id,job_name')
    }
    changeSet(author: 'wanghao', id: '2022-11-25-add-index') {
        createIndex(tableName: 'devops_ci_audit_record', indexName: 'devops_ci_audit_record_n1') {
            column(name: 'ci_pipeline_id')
        }
    }

}