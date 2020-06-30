package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/cicd_job_record.groovy') {
    changeSet(author: 'wanghao', id: '2020-06-30-create-table') {
        createTable(tableName: "cicd_job_record", remarks: 'cicd_job_record') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'gitlab_job_id', type: 'BIGINT UNSIGNED', remarks: 'gitlab job id')
            column(name: 'gitlab_pipeline_id', type: 'BIGINT UNSIGNED', remarks: 'gitlab_流水线记录id')
            column(name: 'cicd_pipeline_record_id', type: 'BIGINT UNSIGNED', remarks: '流水线记录Id')
            column(name: 'metadata', type: 'VARCHAR(2000)', remarks: 'job详细信息，定义了job执行内容')
            column(name: 'name', type: 'VARCHAR(255)', remarks: '任务名称')
            column(name: 'stage', type: 'VARCHAR(255)', remarks: '所属阶段名称')
            column(name: 'status', type: 'VARCHAR(255)', remarks: 'job状态')
            column(name: 'trigger_user_id', type: 'BIGINT UNSIGNED', remarks: '触发用户id')
            column(name: 'trigger_version', type: 'VARCHAR(255)', remarks: '触发版本')
            column(name: "started_date", type: "DATETIME", remarks: 'job开始执行时间')
            column(name: "finished_date", type: "DATETIME", remarks: 'job结束时间')
            column(name: "type", type: "VARCHAR(255)", remarks: '任务类型')
            column(name: "duration_seconds", type: "BIGINT UNSIGNED", remarks: 'job执行时长')
            column(name: "gitlab_project_id", type: "BIGINT UNSIGNED", remarks: 'gitlab_project_id')

            column(name: "app_service_id", type: "BIGINT UNSIGNED", remarks: 'app_service_id')
            column(name: "cicd_job_id", type: "BIGINT UNSIGNED", remarks: 'cicd_job_id')
            column(name: "cicd_stage_record_id", type: "BIGINT UNSIGNED", remarks: 'cicd_stage_record_id')
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境Id')
            column(name: 'instance_id', type: 'BIGINT UNSIGNED', remarks: '实例Id')
            column(name: 'version_id', type: 'BIGINT UNSIGNED', remarks: '版本Id')
            column(name: "project_id", type: "BIGINT UNSIGNED", remarks: 'project_id')
            column(name: "app_service_deploy_id", type: "BIGINT UNSIGNED", remarks: 'app_service_deploy_id')
            column(name: "value_id", type: "BIGINT UNSIGNED", remarks: 'value_id')
            column(name: 'audit_user', type: 'VARCHAR(255)', remarks: '审核人员')
            column(name: 'is_countersigned', type: 'TINYINT UNSIGNED', remarks: '是否会签')
            column(name: 'value', type: 'TEXT', remarks: '配置信息')
            column(name: 'instance_name', type: 'VARCHAR(100)', remarks: '实例名称')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'cicd_job_record',
                constraintName: 'uk_gitlab_job_id', columnNames: 'gitlab_job_id')
    }

//    changeSet(author: 'wanghao', id: '2020-06-30-add-column') {
//        createIndex(tableName: 'cicd_job_record', indexName: 'ci_job_record_gpid_idx') {
//            column(name: 'gitlab_project_id')
//        }
//    }
//    changeSet(author: 'wanghao', id: '2020-06-30-add-not-null-cons') {
//        addNotNullConstraint(tableName: "cicd_job_record", columnName: "name", columnDataType: "VARCHAR(255)")
//    }
}