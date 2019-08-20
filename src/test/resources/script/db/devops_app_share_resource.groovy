package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_app_share_resource.groovy') {
    changeSet(author: 'scp', id: '2019-06-28-create-table') {
        createTable(tableName: "devops_app_share_resource", remarks: '应用共享资源') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'share_id', type: 'BIGINT UNSIGNED', remarks: '共享ID')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目ID')
        }
    }

    changeSet(author: 'sheep', id: '2019-8-05-rename-table') {
        renameTable(newTableName: 'devops_app_service_share_resource', oldTableName: 'devops_app_share_resource')
    }
}
