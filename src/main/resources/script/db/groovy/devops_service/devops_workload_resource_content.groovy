package script.db.groovy.devops_service


databaseChangeLog(logicalFilePath: 'dba/devops_workload_resource_content.groovy') {
    changeSet(author: 'lihao', id: '2021-06-09-create-table') {
        createTable(tableName: "devops_workload_resource_content", remarks: 'workload resource content') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'workload_id', type: 'BIGINT UNSIGNED', remarks: '关联的负载资源id') {
                constraints(nullable: false)
            }
            column(name: 'type', type: 'VARCHAR(32)', remarks: '关联的负载资源类型') {
                constraints(nullable: false)
            }
            column(name: 'content', type: 'TEXT', remarks: 'yaml resource') {
                constraints(nullable: false)
            }
        }

        addUniqueConstraint(tableName: 'devops_workload_resource_content',
                constraintName: 'uk_id_type', columnNames: 'workload_id,type')
    }
}