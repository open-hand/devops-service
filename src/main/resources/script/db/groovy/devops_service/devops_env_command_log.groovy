package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_env_command_log.groovy') {
    changeSet(author: 'Younger', id: '2018-04-24-create-table') {
        createTable(tableName: "devops_env_command_log", remarks: '操作日志表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'command_id', type: 'BIGINT UNSIGNED', remarks: '操作ID')
            column(name: 'log', type: 'TEXT', remarks: '资源日志')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(author: 'crockitwood', id: '2018-09-26-create-command-log-index') {
        createIndex(indexName: "idx_command_id", tableName: "devops_env_command_log") {
            column(name: "command_id")
        }
    }


    changeSet(author: 'Younger', id: '2018-10-08-drop-column') {
        dropColumn(columnName: "object_version_number", tableName: "devops_env_command_log")
        dropColumn(columnName: "created_by", tableName: "devops_env_command_log")
        dropColumn(columnName: "creation_date", tableName: "devops_env_command_log")
        dropColumn(columnName: "last_updated_by", tableName: "devops_env_command_log")
        dropColumn(columnName: "last_update_date", tableName: "devops_env_command_log")
    }
}