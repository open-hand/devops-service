package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_env_resource_detail.groovy') {
    changeSet(author: 'Younger', id: '2018-04-24-create-table') {
        createTable(tableName: "devops_env_resource_detail", remarks: '环境资源详情表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'message', type: 'TEXT', remarks: '资源信息')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}