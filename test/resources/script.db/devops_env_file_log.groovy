package script.db

databaseChangeLog(logicalFilePath: 'db/devops_env_file_log.groovy') {
    changeSet(author: 'Younger', id: '2018-08-01-create-table') {
        createTable(tableName: "devops_env_file_log", remarks: '环境文件报错信息') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }

            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境 ID ') {
                constraints(unique: true)
            }
            column(name: 'file_path', type: 'VARCHAR(512)', remarks: '文件路径')
            column(name: 'commit_sha', type: 'VARCHAR(100)', remarks: '提交')
            column(name: 'message', type: 'VARCHAR(2000)', remarks: '报错信息')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(author: 'Younger', id: '2018-08-01-delete-table') {
        dropTable(tableName: "devops_env_file_log")
    }


}