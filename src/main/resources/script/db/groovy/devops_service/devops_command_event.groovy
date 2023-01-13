package script.db.groovy.devops_service


databaseChangeLog(logicalFilePath: 'dba/devops_command_event.groovy') {
    changeSet(author: 'Younger', id: '2018-07-01-create-table') {
        createTable(tableName: "devops_command_event", remarks: 'command 事件信息表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'command_id', type: 'BIGINT UNSIGNED', remarks: 'devops_env_command表主键Id')
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

    changeSet(author: 'crockitwood', id: '2018-09-26-create-command-event-index') {
        createIndex(indexName: "idx_command_id_type", tableName: "devops_command_event") {
            column(name: "command_id")
            column(name: 'type')
        }
    }

    changeSet(author: 'crockitwood', id: '2018-10-19-create-command-event-id-index') {
        createIndex(indexName: "idx_command_id", tableName: "devops_command_event") {
            column(name: "command_id")
        }
    }

    changeSet(author: 'lihao', id: '2021-11-02-drop-index') {
        dropIndex(indexName: "idx_command_id_type", tableName: "devops_command_event")
    }
    changeSet(author: 'wanghao', id: '2023-01-13-modify-column') {
        modifyDataType(tableName: 'devops_command_event', columnName: 'message', newDataType: 'VARCHAR(5000)')
    }
}