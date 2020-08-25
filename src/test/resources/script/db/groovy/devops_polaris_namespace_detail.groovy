package script.db.groovy

databaseChangeLog(logicalFilePath: 'dba/devops_polaris_namespace_detail.groovy') {
    changeSet(author: 'zmf', id: '2020-02-14-create-table-devops_polaris_namespace_detail') {
        createTable(tableName: "devops_polaris_namespace_detail", remarks: 'namespace详细扫描结果') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'detail', type: 'MEDIUMTEXT', remarks: '是这个namespace下所有扫描数据json，根据扫描范围是env或者是cluster结构会有不同') {
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