package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/cicd_pipeline_record.groovy') {
    changeSet(author: 'wanghao', id: '2020-06-30-create-table') {
        createTable(tableName: "cicd_pipeline_record", remarks: 'cicd_pipeline_record') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'cicd_pipeline_id', type: 'BIGINT UNSIGNED', remarks: '流水线id')
            column(name: "pipeline_name", type: 'VARCHAR(64)', remarks: "pipeline name")
            column(name: 'gitlab_pipeline_id', type: 'BIGINT UNSIGNED', remarks: 'gitlab流水线记录id')
            column(name: 'commit_sha', type: 'VARCHAR(255)', remarks: 'commit_sha')
            column(name: 'gitlab_trigger_ref', type: 'VARCHAR(255)', remarks: '触发分支')
            column(name: 'status', type: 'VARCHAR(255)', remarks: '流水线状态')
            column(name: "gitlab_project_id", type: "BIGINT UNSIGNED", remarks: 'gitlab_project_id')
            column(name: 'trigger_user_id', type: 'BIGINT UNSIGNED', remarks: '触发用户id')
            column(name: "created_date", type: "DATETIME", remarks: 'gitlab流水线创建时间')
            column(name: "finished_date", type: "DATETIME", remarks: 'gitlab流水线结束时间')
            column(name: "duration_seconds", type: "BIGINT UNSIGNED", remarks: '流水线执行时长')

            column(name: 'trigger_type', type: 'VARCHAR(10)', remarks: '触发方式')
            column(name: 'bpm_definition', type: 'TEXT', remarks: 'bpm定义')
            column(name: 'business_key', type: 'VARCHAR(255)', remarks: '流程实例')
            column(name: "edited", type: 'TINYINT UNSIGNED', remarks: "是否编辑", defaultValue: "0")
            column(name: 'audit_user', type: 'VARCHAR(255)', remarks: '审核人员')
            column(name: 'error_info', type: 'VARCHAR(255)', remarks: '错误信息')


            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

    }

}
