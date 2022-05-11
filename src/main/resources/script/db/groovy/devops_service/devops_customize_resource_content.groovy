package script.db.groovy.devops_service


databaseChangeLog(logicalFilePath: 'dba/devops_customize_resource_content.groovy') {
    changeSet(author: 'Sheep', id: '2019-06-26-create-table') {
        createTable(tableName: "devops_customize_resource_content", remarks: '自定义资源内容表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'content', type: 'TEXT', remarks: 'yaml resource')
        }

    }

}