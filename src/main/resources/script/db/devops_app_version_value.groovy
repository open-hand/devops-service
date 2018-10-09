package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_app_version_value.groovy') {
    changeSet(author: 'Runge', id: '2018-05-08-create-table') {
        createTable(tableName: "devops_app_version_value", remarks: '版本参数表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'value', type: 'TEXT', remarks: '参数')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }


    changeSet(author: 'Younger', id: '2018-10-08-drop-column') {
        dropColumn(columnName: "object_version_number", tableName: "devops_app_version_value")
        dropColumn(columnName: "created_by", tableName: "devops_app_version_value")
        dropColumn(columnName: "creation_date", tableName: "devops_app_version_value")
        dropColumn(columnName: "last_updated_by", tableName: "devops_app_version_value")
        dropColumn(columnName: "last_update_date", tableName: "devops_app_version_value")
    }

}