package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_unit_test_report.groovy') {
    changeSet(author: 'wanghao', id: '2021-12-24-create-table') {
        createTable(tableName: "devops_ci_unit_test_report", remarks: '单元测试报告') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'devops_pipeline_id', type: 'BIGINT UNSIGNED', remarks: 'devops流水线id') {
                constraints(nullable: false)
            }
            column(name: 'gitlab_pipeline_id', type: 'BIGINT UNSIGNED', remarks: 'gitlabPipelineId') {
                constraints(nullable: false)
            }
            column(name: "job_name", type: "VARCHAR(255)", remarks: "任务名称") {
                constraints(nullable: false)
            }
            column(name: "type", type: "VARCHAR(255)", remarks: "测试类型") {
                constraints(nullable: false)
            }
            column(name: "tests", type: "BIGINT UNSIGNED", remarks: "测试用例总数") {
                constraints(nullable: false)
            }
            column(name: "failures", type: "BIGINT UNSIGNED", remarks: "失败用例总数") {
                constraints(nullable: false)
            }
            column(name: "skipped", type: "BIGINT UNSIGNED", remarks: "跳过用例总数") {
                constraints(nullable: false)
            }
            column(name: "report_url", type: "VARCHAR(1024)", remarks: "测试报告地址")

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_ci_unit_test_report',
                constraintName: 'uk_devops_gitlab_pipeline_id', columnNames: 'devops_pipeline_id,gitlab_pipeline_id,job_name')
    }
}
