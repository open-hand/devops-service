package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_cd_audit.groovy') {
    changeSet(author: 'wx', id: '2020-06-29-create-table') {
        createTable(tableName: "devops_cd_audit", remarks: '执行关系记录') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'user_id', type: 'BIGINT UNSIGNED', remarks: '用户Id')
            column(name: 'cicd_pipeline_id', type: 'BIGINT UNSIGNED', remarks: '流水线id')
            column(name: 'cicd_stage_id', type: 'BIGINT UNSIGNED', remarks: '阶段Id')
            column(name: 'cicd_job_id', type: 'BIGINT UNSIGNED', remarks: '任务Id')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}