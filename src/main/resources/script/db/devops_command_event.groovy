package script.db


databaseChangeLog(logicalFilePath: 'dba/devops_command_event.groovy') {
    changeSet(author: 'Younger', id: '2018-07-01-create-table') {
        createTable(tableName: "devops_command_event", remarks: 'command event') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'commandId', type: 'BIGINT UNSIGNED', remarks: 'command Id')
            column(name: 'type', type: 'VARCHAR(64)', remarks: '类型')
            column(name: 'name', type: 'VARCHAR(64)', remarks: 'name')
            column(name: 'message', type: 'VARCHAR(2000)', remarks: '信息')
            column(name: 'event_creation_time', type: 'DATETIME', remarks: 'Event时间')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}