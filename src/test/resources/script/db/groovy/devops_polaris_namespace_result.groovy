package script.db.groovy

databaseChangeLog(logicalFilePath: 'dba/devops_polaris_namespace_result.groovy') {
    changeSet(author: 'zmf', id: '2020-02-14-create-table_polaris_namespace_result') {
        createTable(tableName: "devops_polaris_namespace_result", remarks: 'polaris扫描结果的namespace这一级别的数据') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境id / 可为空')
            column(name: 'namespace', type: 'VARCHAR(128)', remarks: '集群namespace') {
                constraints(nullable: false)
            }
            column(name: 'record_id', type: 'BIGINT UNSIGNED', remarks: '扫描纪录id') {
                constraints(nullable: false)
            }
            column(name: 'detail_id', type: 'BIGINT UNSIGNED', remarks: '此条资源详细扫描纪录id') {
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

        createIndex(indexName: "idx_polaris-namespace-result_record_id ", tableName: "devops_polaris_namespace_result") {
            column(name: "record_id")
        }
        createIndex(indexName: "idx_polaris-namespace-result_env_id ", tableName: "devops_polaris_namespace_result") {
            column(name: "env_id")
        }
    }
}