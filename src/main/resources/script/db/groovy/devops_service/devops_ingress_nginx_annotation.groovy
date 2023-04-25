package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ingress_nginx_annotation.groovy') {
    changeSet(author: 'wanghao', id: '2023-04-25-create-table') {
        createTable(tableName: "devops_ingress_nginx_annotation", remarks: 'Nginx-Ingress注解配置') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'ingress_id', type: 'BIGINT UNSIGNED', remarks: 'devops_ingress.id')
            column(name: 'key', type: 'VARCHAR(255)', remarks: 'key')
            column(name: 'value', type: 'VARCHAR(512)', remarks: 'value')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(tableName: 'devops_ingress_nginx_annotation', indexName: 'devops_ingress_nginx_annotation_n1') {
            column(name: 'ingress_id')
        }
        addUniqueConstraint(tableName: 'devops_ingress_nginx_annotation', constraintName: 'devops_ingress_nginx_annotation_u1', columnNames: 'ingress_id,key')
    }

}