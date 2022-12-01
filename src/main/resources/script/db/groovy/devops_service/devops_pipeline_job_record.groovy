package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_pipeline_job_record.groovy') {
    changeSet(author: 'wanghao', id: '2022-11-23-create-table') {
        createTable(tableName: "devops_pipeline_job_record", remarks: '流水线任务记录') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(64)', remarks: '名称') {
                constraints(nullable: false)
            }
            column(name: 'pipeline_id', type: 'BIGINT UNSIGNED', remarks: '所属流水线Id,devops_pipeline.id') {
                constraints(nullable: false)
            }
            column(name: 'job_id', type: 'BIGINT UNSIGNED', remarks: '所属任务Id,devops_pipeline_job.id') {
                constraints(nullable: false)
            }
            column(name: 'pipeline_record_id', type: 'BIGINT UNSIGNED', remarks: '关联流水线记录Id,devops_pipeline_record.id') {
                constraints(nullable: false)
            }
            column(name: 'stage_record_id', type: 'BIGINT UNSIGNED', remarks: '关联阶段记录Id,devops_pipeline_stage_record.id') {
                constraints(nullable: false)
            }
            column(name: 'status', type: 'VARCHAR(20)', remarks: '状态') {
                constraints(nullable: false)
            }
            column(name: "started_date", type: "DATETIME", remarks: 'job开始执行时间')

            column(name: "finished_date", type: "DATETIME", remarks: 'job结束时间')
            column(name: 'type', type: 'VARCHAR(64)', remarks: '任务类型') {
                constraints(nullable: false)
            }
            column(name: 'log_id', type: 'BIGINT UNSIGNED', remarks: '关联日志记录Id,devops_pipeline_log.id')
            column(name: 'command_id', type: 'BIGINT UNSIGNED', remarks: '部署操作commandId')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(tableName: 'devops_pipeline_job_record', indexName: 'devops_pipeline_job_record_n1') {
            column(name: 'stage_record_id')
        }
        createIndex(tableName: 'devops_pipeline_job_record', indexName: 'devops_pipeline_job_record_n2') {
            column(name: 'pipeline_id')
        }
    }

}