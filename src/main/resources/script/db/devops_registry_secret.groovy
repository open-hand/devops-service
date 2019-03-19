package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_registry_secret.groovy') {
    changeSet(author: 'Sheep', id: '2019-03-14-create-table') {
        createTable(tableName: "devops_registry_secret", remarks: '私有镜像仓库密钥') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境Id')
            column(name: 'config_id', type: 'BIGINT UNSIGNED', remarks: '配置Id')
            column(name: 'secret_code', type: 'VARCHAR(32)', remarks: 'secret编码')
            column(name: 'secret_detail', type: 'VARCHAR(64)', remarks: 'secret内容')
            column(name: 'status', type: 'VARCHAR(32)', remarks: 'secret状态')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}
