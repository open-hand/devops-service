package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_pipeline_user_record_rel.groovy') {
    changeSet(author: 'scp', id: '2019-04-03-create-table') {
        createTable(tableName: "devops_pipeline_user_record_rel", remarks: '执行关系记录') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'user_id', type: 'BIGINT UNSIGNED', remarks: '用户Id')
            column(name: 'pipeline_record_id', type: 'BIGINT UNSIGNED', remarks: '流水线记录Id')
            column(name: 'stage_record_id', type: 'BIGINT UNSIGNED', remarks: '阶段记录Id')
            column(name: 'task_record_id', type: 'BIGINT UNSIGNED', remarks: '任务记录Id')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(author: 'scp', id: '2019-06-04-idx-project-id') {
        createIndex(indexName: "idx_task_record_id ", tableName: "devops_pipeline_user_record_rel") {
            column(name: "task_record_id")
        }
        createIndex(indexName: "idx_stage_record_id ", tableName: "devops_pipeline_user_record_rel") {
            column(name: "stage_record_id")
        }
    }
}