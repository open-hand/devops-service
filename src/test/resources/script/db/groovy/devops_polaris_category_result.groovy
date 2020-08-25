package script.db.groovy

databaseChangeLog(logicalFilePath: 'dba/devops_polaris_category_result.groovy') {
    changeSet(author: 'zmf', id: '2020-02-14-create-table-polaris_category_result') {
        createTable(tableName: "devops_polaris_category_result", remarks: 'polaris扫描结果的按照category划分的纪录') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'record_id', type: 'BIGINT UNSIGNED', remarks: '扫描纪录id') {
                constraints(nullable: false)
            }
            column(name: 'score', type: 'BIGINT UNSIGNED', remarks: '扫描结果的得分') {
                constraints(nullable: false)
            }
            column(name: 'category', type: 'VARCHAR(45)', remarks: '这个type所属的类型') {
                constraints(nullable: false)
            }
            column(name: 'detail_id', type: 'BIGINT UNSIGNED', remarks: '详情id') {
                constraints(nullable: false)
            }
            column(name: 'has_errors', type: 'TINYINT UNSIGNED', remarks: '是否有error级别的检测项') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(indexName: "idx_polaris_category_record_id ", tableName: "devops_polaris_category_result") {
            column(name: "record_id")
        }
    }
}