package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_pipeline_function.groovy') {
    changeSet(author: 'wanghao', id: '20221-11-15-create-table') {
        createTable(tableName: "devops_ci_pipeline_function", remarks: 'devops_ci_pipeline') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(255)', remarks: '流水线名称')
            column(name: 'devops_pipeline_id', type: 'BIGINT UNSIGNED', remarks: 'devops流水线id/为0代表平台默认')
            column(name: 'script', type: 'TEXT', remarks: '函数脚本')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_ci_pipeline_function',
                constraintName: 'uk_function_name', columnNames: 'devops_pipeline_id,name')

        createIndex(indexName: "idx_pipeline_id", tableName: "devops_ci_pipeline_function") {
            column(name: "devops_pipeline_id")
        }
    }
}
