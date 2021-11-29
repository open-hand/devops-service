package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_pipeline_template_job_group.groovy') {
    changeSet(author: 'wx', id: '2021-11-29-create-table-devops_pipeline_template_job_group') {
        createTable(tableName: "devops_pipeline_template_job_group", remarks: '流水线任务模板分组') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(30)', remarks: '任务名称') {
                constraints(nullable: false)
            }

            column(name: 'source_type', type: 'VARCHAR(12)', remarks: '层级') {
                constraints(nullable: false)
            }
            column(name: 'source_id',  type: 'BIGINT UNSIGNED', remarks: '层级Id') {
                constraints(nullable: false)
            }
            column(name: 'built_in',  type: 'TINYINT UNSIGNED', remarks: '是否预置，1:预置，0:自定义') {
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