package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_env_command.groovy') {
    changeSet(author: 'Younger', id: '2018-05-15-create-table') {
        createTable(tableName: "devops_env_command", remarks: '对象操作') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'object', type: 'VARCHAR(32)', remarks: '操作对象')
            column(name: 'object_id', type: 'BIGINT UNSIGNED', remarks: '操作对象ID')
            column(name: 'value_id', type: 'BIGINT UNSIGNED', remarks: '参数ID ')
            column(name: 'command_type', type: 'VARCHAR(32)', remarks: '操作类型')
            column(name: 'status', type: 'VARCHAR(32)', remarks: '操作状态')
            column(name: 'error', type: 'VARCHAR(5000)', remarks: '错误信息')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(author: 'younger', id: '2018-09-10-add-column')
            {
                addColumn(tableName: 'devops_env_command') {
                    column(name: 'sha', type: 'VARCHAR(128)', remarks: 'commit sha', afterColumn: 'status')
                }
            }
    changeSet(author: 'younger', id: '2018-10-23-add-column')
            {
                addColumn(tableName: 'devops_env_command') {
                    column(name: 'object_version_id', type: 'BIGINT UNSIGNED', remarks: 'object version id', afterColumn: 'object_id')
                }

            }
}