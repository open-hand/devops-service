package script.db

databaseChangeLog(logicalFilePath: 'db/devops_env_file_resource.groovy') {
    changeSet(author: 'Runge', id: '2018-07-25-create-table') {
        createTable(tableName: "devops_env_file_resource", remarks: '环境文件信息') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }

            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境 ID ') {
            }
            column(name: 'file_path', type: 'VARCHAR(512)', remarks: '文件路径')
            column(name: 'resource_type', type: 'VARCHAR(32)', remarks: '资源类型')
            column(name: 'resource_id', type: 'BIGINT UNSIGNED', remarks: '资源ID')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }


}