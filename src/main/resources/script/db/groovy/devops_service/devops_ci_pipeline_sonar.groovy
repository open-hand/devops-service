package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_pipeline_sonar.groovy') {
    changeSet(author: 'wanghao', id: '2021-12-15-create-table') {
        createTable(tableName: "devops_ci_pipeline_sonar", remarks: 'ci任务生成sonar记录') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'app_service_id', type: 'BIGINT UNSIGNED', remarks: '应用服务id') {
                constraints(nullable: false)
            }
            column(name: 'gitlab_pipeline_id', type: 'BIGINT UNSIGNED', remarks: 'gitlabPipelineId') {
                constraints(nullable: false)
            }
            column(name: "job_name", type: "VARCHAR(255)", remarks: "任务名称") {
                constraints(nullable: false)
            }
            column(name: "scanner_type", type: "VARCHAR(255)", remarks: "sonar扫描器类型") {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_ci_pipeline_sonar',
                constraintName: 'uk_devops_gitlab_pipeline_id', columnNames: 'app_service_id,gitlab_pipeline_id,job_name')
    }
    changeSet(author: 'wanghao', id: '2023-6-6-add-column') {
        addColumn(tableName: 'devops_ci_pipeline_sonar') {
            column(name: "record_id", type: "BIGINT UNSIGNED", afterColumn: "job_name", remarks: "devops_sonar_analyse_record.id")
        }
    }
}
