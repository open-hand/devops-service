package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_step.groovy') {
    changeSet(author: 'wanghao', id: '2021-11-29-create-table') {
        createTable(tableName: "devops_ci_step", remarks: 'CI步骤表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目ID')

            column(name: 'name', type: 'VARCHAR(255)', remarks: '步骤名称') {
                constraints(nullable: false)
            }
            column(name: 'devops_ci_job_id', type: 'BIGINT UNSIGNED', remarks: 'devops流水线任务id') {
                constraints(nullable: false)
            }

            column(name: 'type', type: 'VARCHAR(255)', remarks: '任务类型 build 构建，sonar 代码检查, chart chart发布, custom 自定义') {
                constraints(nullable: false)
            }

            column(name: 'script', type: 'TEXT', remarks: '步骤中包含的脚本')

            column(name: 'sequence', type: 'BIGINT UNSIGNED', remarks: '步骤的顺序') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(indexName: "idx_devops_ci_job_id ", tableName: "devops_ci_step") {
            column(name: "devops_ci_job_id")
        }
    }

}