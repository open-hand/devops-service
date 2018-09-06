package script.db


databaseChangeLog(logicalFilePath: 'db/devops_env_file.groovy') {
    changeSet(author: 'Younger', id: '2018-08-01-create-table') {
        createTable(tableName: "devops_env_file", remarks: '环境文件') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境 ID ')
            column(name: 'file_path', type: 'VARCHAR(512)', remarks: '文件路径')
            column(name: 'commit_sha', type: 'VARCHAR(100)', remarks: '提交')
            column(name: 'message', type: 'VARCHAR(2000)', remarks: '报错信息')
            column(name: 'is_sync', type: 'TINYINT UNSIGNED', remarks: '是否同步')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(id: '2018-08-09-drop-column', author: 'younger') {
        dropColumn(columnName: "message", tableName: "devops_env_file")
        dropColumn(columnName: "is_sync",tableName: "devops_env_file")
        renameColumn(columnDataType: 'VARCHAR(100)', newColumnName: 'devops_commit', oldColumnName: 'commit_sha', remarks: 'devops sync commit', tableName: 'devops_env_file')
        addColumn(tableName: 'devops_env_file') {
            column(name: 'agent_commit', type: 'VARCHAR(512)', remarks: 'agent sync commit', afterColumn: 'devops_commit')
        }
    }
}