package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_hzero_deploy_value.groovy') {
    changeSet(author: 'wanghao', id: '2021-07-27-create-table') {
        createTable(tableName: "devops_hzero_deploy_value", remarks: 'hzero部署配置') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'value', type: 'TEXT', remarks: '部署配置')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}