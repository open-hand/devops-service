package script.db


databaseChangeLog(logicalFilePath: 'dba/devops_cluster_pro_permission.groovy') {
    changeSet(author: 'Younger', id: '2018-11-01-create-table') {
        createTable(tableName: "devops_cluster_pro_permission", remarks: 'cluster project permission') {
            column(name: 'cluster_id', type: 'BIGINT UNSIGNED', remarks: '集群Id')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目Id')
            column(name: 'project_name', type: 'BIGINT UNSIGNED', remarks: '项目名')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }


}