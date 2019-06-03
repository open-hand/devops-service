package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_pipeline_app_deploy.groovy') {
    changeSet(author: 'scp', id: '2019-04-03-create-table') {
        createTable(tableName: "devops_pipeline_app_deploy", remarks: '流水线应用部署') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'application_id', type: 'BIGINT UNSIGNED', remarks: '应用Id')
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境Id')
            column(name: 'value_id', type: 'BIGINT UNSIGNED', remarks: 'valueId')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目ID')
            column(name: 'instance_id', type: 'BIGINT UNSIGNED', remarks: '实例ID')
            column(name: 'trigger_version', type: 'VARCHAR(100)', remarks: '触发版本')
            column(name: 'instance_name', type: 'VARCHAR(50)', remarks: '实例名称')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}