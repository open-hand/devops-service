package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_notification.groovy') {
    changeSet(author: 'scp', id: '2019-05-13-create-table') {
        createTable(tableName: "devops_notification", remarks: '通知设置') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目ID')
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境 ID')
            column(name: 'notify_type', type: 'VARCHAR(64)', remarks: '通知方式')
            column(name: 'notify_object', type: 'VARCHAR(64)', remarks: '通知对象')
            column(name: 'notify_trigger_event', type: 'VARCHAR(64)', remarks: '触发事件')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}