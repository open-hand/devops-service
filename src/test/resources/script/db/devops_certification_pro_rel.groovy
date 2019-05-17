package script.db


databaseChangeLog(logicalFilePath: 'dba/devops_certification_pro_rel.groovy') {
    changeSet(author: 'Younger', id: '2018-12-10-create-table') {
        createTable(tableName: "devops_certification_pro_rel", remarks: '证书项目关联关系表') {
            column(name: 'cert_id', type: 'BIGINT UNSIGNED', remarks: '集群Id')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目Id')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

}