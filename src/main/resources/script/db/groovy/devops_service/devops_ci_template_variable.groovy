package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_template_variable.groovy') {
    changeSet(author: 'wx', id: '2021-11-29-create-table-devops_ci_template_variable') {
        createTable(tableName: "devops_ci_template_variable", remarks: '流水线模板配置的CI变量') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }

            column(name: 'pipeline_template_id',  type: 'BIGINT UNSIGNED', remarks: '流水线模板id') {
                constraints(nullable: false)
            }

            column(name: 'variable_key', type: 'VARCHAR(255)', remarks: '层级') {
                constraints(nullable: false)
            }
            column(name: 'variable_value', type: 'VARCHAR(255)', remarks: '层级') {
                constraints(nullable: false)
            }


            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")

        }

    }
    changeSet(author: 'wanghao', id: '2023-02-15-updateDataType') {
        modifyDataType(tableName: 'devops_ci_template_variable', columnName: 'variable_value', newDataType: 'TEXT')
    }
}