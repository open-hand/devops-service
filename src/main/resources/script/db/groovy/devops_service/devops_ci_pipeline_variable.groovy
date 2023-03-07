package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_pipeline_variable.groovy') {
    changeSet(author: 'wanghao', id: '2021-11-29-create-table') {
        createTable(tableName: "devops_ci_pipeline_variable", remarks: '流水线配置的CI变量') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }

            column(name: 'devops_pipeline_id', type: 'BIGINT UNSIGNED', remarks: '流水线id') {
                constraints(nullable: false)
            }

            column(name: 'variable_key', type: 'VARCHAR(255)', remarks: '变量名') {
                constraints(nullable: false)
            }
            column(name: 'variable_value', type: 'VARCHAR(255)', remarks: '变量值') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")

        }
        addUniqueConstraint(tableName: 'devops_ci_pipeline_variable',
                constraintName: 'uk_key_value', columnNames: 'devops_pipeline_id,variable_key')
        createIndex(tableName: 'devops_ci_pipeline_variable', indexName: 'idx_pipeline_id') {
            column(name: 'devops_pipeline_id')
        }
    }
    changeSet(author: 'wanghao', id: '2023-02-15-updateDataType') {
        modifyDataType(tableName: 'devops_ci_pipeline_variable', columnName: 'variable_value', newDataType: 'TEXT')
    }
}