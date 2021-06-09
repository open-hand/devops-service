package script.db.groovy.devops_service


databaseChangeLog(logicalFilePath: 'dba/devops_workload_resource_content.groovy') {
    changeSet(author: 'Sheep', id: '2021-06-09-create-table') {
        createTable(tableName: "devops_workload_resource_content", remarks: 'workload resource content') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'workload_id', type: 'UNSIGNED BIGINT', remarks: '关联的负载资源id')
            column(name: 'type', type: 'VARCHAR(32)', remarks: '关联的负载资源类型')
            column(name: 'content', type: 'TEXT', remarks: 'yaml resource')
        }

        addUniqueConstraint(tableName: 'devops_workload_resource_content',
                constraintName: 'uk_id_type', columnNames: 'workload_id,type')
    }
}