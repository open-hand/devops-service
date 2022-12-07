package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_pipeline_record.groovy') {
    changeSet(author: 'wanghao', id: '2022-11-23-create-table') {
        createTable(tableName: "devops_pipeline_record", remarks: '流水线执行记录') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'pipeline_id', type: 'BIGINT UNSIGNED', remarks: '所属流水线Id,devops_pipeline.id') {
                constraints(nullable: false)
            }
            column(name: 'name', type: 'VARCHAR(64)', remarks: '流水线名称') {
                constraints(nullable: false)
            }
            column(name: 'status', type: 'VARCHAR(20)', remarks: '状态') {
                constraints(nullable: false)
            }
            column(name: "started_date", type: "DATETIME", remarks: '流水线开始执行时间')

            column(name: "finished_date", type: "DATETIME", remarks: '流水线结束时间')
            column(name: 'trigger_type', type: 'VARCHAR(20)', remarks: '触发方式') {
                constraints(nullable: false)
            }
            column(name: 'app_service_id', type: 'BIGINT UNSIGNED', remarks: '触发应用服务id,devops_app_service.id')
            column(name: 'app_service_version_id', type: 'BIGINT UNSIGNED', remarks: '触发应用服务版本id,devops_app_service_version.id')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(tableName: 'devops_pipeline_record', indexName: 'devops_pipeline_record_n1') {
            column(name: 'pipeline_id')
        }
    }

}
