package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_template_stage_job_rel.groovy') {
    changeSet(author: 'wx', id: '2021-11-29-create-table-devops_ci_template_stage_job_rel') {
        createTable(tableName: "devops_ci_template_stage_job_rel", remarks: '流水线阶段与任务模板的关系表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'ci_template_stage_id', type: 'BIGINT UNSIGNED', remarks: '流水线模板阶段id') {
                constraints(nullable: false)
            }

            column(name: 'ci_template_job_id', type: 'BIGINT UNSIGNED', remarks: '流水线模板id') {
                constraints(nullable: false)
            }


            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")

        }
        addUniqueConstraint(tableName: 'devops_ci_template_stage_job_rel', constraintName: 'uk_stage_job_id', columnNames: 'ci_template_stage_id,ci_template_job_id')
        createIndex(tableName: 'devops_ci_template_stage_job_rel', indexName: 'idx_ci_template_stage_id') {
            column(name: 'ci_template_stage_id')
        }
        createIndex(tableName: 'devops_ci_template_stage_job_rel', indexName: 'idx_ci_template_job_id') {
            column(name: 'ci_template_job_id')
        }

    }


    changeSet(author: 'lihao', id: '2022-12-26-modify-UniqueConstraint') {
        addColumn(tableName: 'devops_ci_template_stage_job_rel') {
            column(name: 'sequence', type: 'INT(2)', defaultValue: "0", remarks: '顺序') {
                constraints(nullable: false)
            }
        }

        dropUniqueConstraint(constraintName: "uk_stage_job_id", tableName: "devops_ci_template_stage_job_rel")
        addUniqueConstraint(tableName: 'devops_ci_template_stage_job_rel',
                constraintName: 'uk_stage_job_id', columnNames: 'ci_template_stage_id,ci_template_job_id,sequence')
    }
    changeSet(author: 'wanghao', id: '2023-04-03-add-column') {
        addColumn(tableName: 'devops_ci_template_stage_job_rel') {
            column(name: "is_enabled", type: "TINYINT UNSIGNED", defaultValue: "1", afterColumn: 'ci_template_job_id', remarks: '是否启用')
        }
    }
}