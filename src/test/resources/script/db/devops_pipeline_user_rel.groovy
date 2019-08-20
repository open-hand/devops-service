package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_pipeline_user_rel.groovy') {
    changeSet(author: 'scp', id: '2019-04-03-create-table') {
        createTable(tableName: "devops_pipeline_user_rel", remarks: '执行关系记录') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'user_id', type: 'BIGINT UNSIGNED', remarks: '用户Id')
            column(name: 'pipeline_id', type: 'BIGINT UNSIGNED', remarks: '流水线Id')
            column(name: 'stage_id', type: 'BIGINT UNSIGNED', remarks: '阶段Id')
            column(name: 'task_id', type: 'BIGINT UNSIGNED', remarks: '任务Id')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(author: 'scp', id: '2019-06-04-idx-project-id') {
        createIndex(indexName: "devops_pur_idx_pipeline_id ", tableName: "devops_pipeline_user_rel") {
            column(name: "pipeline_id")
            column(name: "user_id")
        }
    }
}