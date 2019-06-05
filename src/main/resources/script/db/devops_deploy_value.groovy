package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_pipeline_value.groovy') {
    changeSet(author: 'scp', id: '2019-04-010-create-table') {
        createTable(tableName: "devops_pipeline_value", remarks: 'value ID') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'value', type: 'TEXT', remarks: '参数')
            column(name: "project_id", type: "BIGINT UNSIGNED", remarks: "项目Id")
            column(name: "app_id", type: "BIGINT UNSIGNED", remarks: "应用Id")
            column(name: "env_id", type: "BIGINT UNSIGNED", remarks: "环境Id")
            column(name: "description", type: "VARCHAR(255)", remarks: "描述")
            column(name: "name", type: "VARCHAR(50)", remarks: "名称")

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
    changeSet(id: '2019-06-05-rename-table', author: 'scp') {
        renameTable(newTableName: 'devops_deploy_value', oldTableName: 'devops_pipeline_value')
    }

}
