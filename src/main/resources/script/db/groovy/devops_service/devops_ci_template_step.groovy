package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_template_step.groovy') {
    changeSet(author: 'wx', id: '2021-11-29-create-table-devops_ci_template_step') {
        createTable(tableName: "devops_ci_template_step", remarks: '流水线步骤模板') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(60)', remarks: '任务名称') {
                constraints(nullable: false)
            }

            column(name: 'source_type', type: 'VARCHAR(12)', remarks: '层级') {
                constraints(nullable: false)
            }
            column(name: 'source_id', type: 'BIGINT UNSIGNED', remarks: '层级Id') {
                constraints(nullable: false)
            }

            column(name: 'category_id', type: 'BIGINT UNSIGNED', remarks: '流水线步骤分类id') {
                constraints(nullable: false)
            }

            column(name: 'type', type: 'VARCHAR(20)', remarks: '步骤类型') {
                constraints(nullable: false)
            }
            column(name: 'script', type: 'text', remarks: '自定义步骤的脚本') {
                constraints(nullable: false)
            }
            column(name: 'config_id', type: 'BIGINT UNSIGNED', remarks: '具体步骤参数配置Id') {
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