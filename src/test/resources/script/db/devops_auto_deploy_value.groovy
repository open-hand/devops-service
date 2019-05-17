package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_auto_deploy_value.groovy') {
    changeSet(author: 'scp', id: '2019-02-26-create-table') {
        createTable(tableName: "devops_auto_deploy_value", remarks: 'value ID') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'value', type: 'TEXT', remarks: '参数')
        }
    }

    changeSet(author: 'scp', id: '2019-03-04-add-column') {
        addColumn(tableName: 'devops_auto_deploy_value') {
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}
