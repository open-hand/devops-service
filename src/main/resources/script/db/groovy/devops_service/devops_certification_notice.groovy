package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'db/devops_certification_notice.groovy') {
    changeSet(author: 'lihao', id: '2023-03-27-create-table') {
        createTable(tableName: "devops_certification_notice", remarks: 'C7N Certification notify') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }

            column(name: 'type', type: 'VARCHAR(32)', remarks: '通知对象类型 role/user')
            column(name: 'object_id', type: 'BIGINT UNSIGNED', remarks: '通知对象id')
            column(name: 'certification_id', type: 'BIGINT UNSIGNED', remarks: '证书id')


            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        createIndex(indexName: "idx_certification_id ", tableName: "devops_certification_notice") {
            column(name: "certification_id")
        }
    }

}