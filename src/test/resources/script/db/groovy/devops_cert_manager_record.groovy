package script.db.groovy
databaseChangeLog(logicalFilePath: 'dba/devops_cert_manager_record.groovy') {
    changeSet(author: 'ztx', id: '2019-11-01-create-table') {
        createTable(tableName: "devops_cert_manager_record", remarks: 'cert-manager record') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'status', type: 'VARCHAR(32)', remarks: '状态')
            column(name: 'error', type: 'text', remarks: '错误信息')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}