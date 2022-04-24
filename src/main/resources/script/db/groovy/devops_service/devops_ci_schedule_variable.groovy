package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_schedule_variable.groovy') {
    changeSet(author: 'wanghao', id: '2022-03-24-create-table') {
        createTable(tableName: "devops_ci_schedule_variable", remarks: 'devops_ci_schedule_variable') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'ci_pipeline_schedule_id', type: 'BIGINT UNSIGNED', remarks: 'devops 定时计划id') {
                constraints(nullable: false)
            }
            column(name: 'pipeline_schedule_id', type: 'BIGINT UNSIGNED', remarks: 'gitlab pipeline_schedule_id')  {
                constraints(nullable: false)
            }
            column(name: 'variable_key', type: 'VARCHAR(255)', remarks: 'key') {
                constraints(nullable: false)
            }

            column(name: 'variable_value', type: 'VARCHAR(255)', remarks: 'value') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_ci_schedule_variable',
                constraintName: 'uk_name', columnNames: 'ci_pipeline_schedule_id,variable_key')

        createIndex(indexName: "idx_ci_pipeline_schedule_id", tableName: "devops_ci_schedule_variable") {
            column(name: "ci_pipeline_schedule_id")
        }
    }
}
