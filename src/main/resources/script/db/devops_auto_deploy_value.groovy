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
}
