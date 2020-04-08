package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_ci_job.groovy') {
    changeSet(author: 'wanghao', id: '2020-04-02-create-table') {
        createTable(tableName: "devops_ci_job", remarks: 'devops_ci_job') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(255)', remarks: '任务名称')
            column(name: 'ci_pipeline_id', type: 'BIGINT UNSIGNED', remarks: '流水线id')
            column(name: 'ci_stage_id', type: 'BIGINT UNSIGNED', remarks: '阶段id')
            column(name: 'type', type: 'VARCHAR(255)', remarks: '任务类型 build 构建，sonar 代码检查')
            column(name: 'trigger_refs', type: 'VARCHAR(255)', remarks: '触发分支')
            column(name: 'metadata', type: 'VARCHAR(2000)', remarks: 'job详细信息，定义了job执行内容')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_ci_job',
                constraintName: 'uk_pipeline_id_job_name', columnNames: 'ci_pipeline_id,name')
    }
    changeSet(author: 'wanghao', id: '2020-04-08-change-column') {
        modifyDataType(tableName: 'devops_ci_job', columnName: 'metadata', newDataType: 'TEXT')
    }
}