package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_pipeline_trigger_config.groovy') {
    changeSet(author: 'lihao', id: '2023-03-03-create-table') {
        createTable(tableName: "devops_ci_pipeline_trigger_config", remarks: 'CI任务表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'job_id', type: 'BIGINT UNSIGNED', remarks: '流水线job id')
            column(name: 'triggered_pipeline_project_id', type: 'BIGINT UNSIGNED', remarks: '触发的其它流水线所属项目id')
            column(name: 'triggered_pipeline_id', type: 'BIGINT UNSIGNED', remarks: '触发的其它流水线id')
            column(name: 'triggered_pipeline_gitlab_project_id', type: 'BIGINT UNSIGNED', remarks: '触发的其它流水线gitlab 项目 id')
            column(name: 'pipeline_trigger_id', type: 'BIGINT UNSIGNED', remarks: '流水线的trigger id')
            column(name: 'ref_name', type: 'VARCHAR(128)', remarks: '触发流水线的分支名称')
            column(name: 'token', type: 'VARCHAR(128)', remarks: 'token')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}