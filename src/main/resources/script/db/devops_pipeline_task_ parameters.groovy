package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_pipeline_task_parameters.groovy') {
    changeSet(author: 'scp', id: '2019-04-03-create-table') {
        createTable(tableName: "devops_pipeline_task_parameters", remarks: '阶段') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'type', type: 'VARCHAR(50)', remarks: '类型')
            column(name: 'task_record_id', type: 'BIGINT UNSIGNED', remarks: '任务记录Id')
            column(name: 'value', type: 'TEXT', remarks: 'value')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}