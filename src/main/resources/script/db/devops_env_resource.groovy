package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_env_resource.groovy') {
    changeSet(author: 'Younger', id: '2018-04-24-create-table') {
        createTable(tableName: "devops_env_resource", remarks: '部署') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'app_instance_id', type: 'BIGINT UNSIGNED', remarks: '部署实例 ID')
            column(name: 'message_id', type: 'BIGINT UNSIGNED', remarks: '资源信息id')
            column(name: 'kind', type: 'VARCHAR(32)', remarks: '资源类型')
            column(name: 'name', type: 'VARCHAR(64)', remarks: '资源名')
            column(name: 'weight ', type: 'BIGINT UNSIGNED', remarks: 'hook执行顺序')
            column(name: 'reversion ', type: 'BIGINT UNSIGNED', remarks: '判断对象是否更新')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(id: '2018-10-08-rename-column', author: 'younger') {
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'resource_detail_id', oldColumnName: 'message_id', remarks: '资源信息', tableName: 'devops_env_resource')
    }
}