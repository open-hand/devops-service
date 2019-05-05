package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_pipeline_app_deploy_value.groovy') {
    changeSet(author: 'scp', id: '2019-04-08-create-table') {
        createTable(tableName: "devops_pipeline_app_deploy_value", remarks: 'value ID') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'value', type: 'TEXT', remarks: '参数')
            column(name: "value_id", type: "BIGINT UNSIGNED", remarks: "valueId")
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
    changeSet(author: 'scp', id: '2019-04-23-delete-table') {
        dropTable(tableName: "devops_pipeline_app_deploy_value")
    }
}
