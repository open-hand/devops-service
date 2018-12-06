package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_secret.groovy') {
    changeSet(author: 'n1ck', id: '2018-12-04-create-table') {
        createTable(tableName: "devops_secret", remarks: 'k8s秘钥表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，密钥对id', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境id')
            column(name: 'name', type: 'VARCHAR(32)', remarks: '秘钥名')
            column(name: 'description', type: 'VARCHAR(32)', remarks: '秘钥描述')
            column(name: 'secret_maps', type: 'VARCHAR(512)', remarks: '秘钥键值对')
            column(name: 'command_id', type: 'BIGINT UNSIGNED', remarks: '操作ID')
            column(name: 'status', type: 'VARCHAR(32)', remarks: '秘钥状态')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        addUniqueConstraint(tableName: 'devops_secret',
                constraintName: 'uk_env_id_name', columnNames: 'env_id,name')

        createIndex(indexName: "idx_name", tableName: "devops_secret") {
            column(name: "name")
        }
    }
}