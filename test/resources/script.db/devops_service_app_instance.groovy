package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_service_app_instance.groovy') {
    changeSet(author: 'Zenger', id: '2018-04-19-create-table') {
        createTable(tableName: "devops_service_app_instance", remarks: '网络实例参数') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'service_id', type: 'BIGINT UNSIGNED', remarks: '网络ID')
            column(name: 'app_instance_id', type: 'BIGINT UNSIGNED', remarks: '实例 ID')
            column(name: 'code', type: 'VARCHAR(64)', remarks: '实例code')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        addUniqueConstraint(tableName: 'devops_service_app_instance', constraintName: 'uk_service_instance_id',
                columnNames: 'service_id,app_instance_id')
    }
}