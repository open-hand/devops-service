package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_polaris_result_detail.groovy') {
    changeSet(author: 'zmf', id: '2020-02-14-create-table-devops_polaris_result_detail') {
        createTable(tableName: "devops_polaris_result_detail", remarks: '资源详细扫描结果') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'detail', type: 'TEXT', remarks: '资源的详细扫描数据') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}