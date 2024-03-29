package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_template_job_step_rel.groovy') {
    changeSet(author: 'wx', id: '2021-11-29-create-table-devops_ci_template_job_step_rel') {
        createTable(tableName: "devops_ci_template_job_step_rel", remarks: '流水线任务模板与步骤模板关系表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }

            column(name: 'ci_template_job_id', type: 'BIGINT UNSIGNED', remarks: '层级Id') {
                constraints(nullable: false)
            }
            column(name: 'ci_template_step_id', type: 'BIGINT UNSIGNED', remarks: '层级Id') {
                constraints(nullable: false)
            }
            column(name: 'sequence', type: 'BIGINT UNSIGNED', remarks: '顺序') {
                constraints(nullable: false)
            }


            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")

        }
        addUniqueConstraint(tableName: 'devops_ci_template_job_step_rel', constraintName: 'uk_job_step_id', columnNames: 'ci_template_job_id,ci_template_step_id')

    }
}