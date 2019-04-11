package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_pipeline_task_record.groovy') {
    changeSet(author: 'scp', id: '2019-04-03-create-table') {
        createTable(tableName: "devops_pipeline_task_record", remarks: '任务记录') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'stage_record_id', type: 'BIGINT UNSIGNED', remarks: '阶段记录Id')
            column(name: 'task_id', type: 'BIGINT UNSIGNED', remarks: '任务Id')
            column(name: 'task_type', type: 'VARCHAR(10)', remarks: '任务类型')
            column(name: 'status', type: 'VARCHAR(10)', remarks: '状态')
            column(name: 'trigger_version', type: 'VARCHAR(255)', remarks: '触发版本')
            column(name: 'application_id', type: 'BIGINT UNSIGNED', remarks: '应用Id')
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境Id')
            column(name: 'instance_id', type: 'BIGINT UNSIGNED', remarks: '实例Id')
            column(name: 'version_id', type: 'BIGINT UNSIGNED', remarks: '版本Id')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目ID')
            column(name: 'app_deploy_id', type: 'BIGINT UNSIGNED', remarks: '应用部署Id')
            column(name: 'execution_time', type: "DATETIME",remarks: '执行时间')
            column(name: 'is_countersigned', type: 'TINYINT UNSIGNED', remarks: '是否会签')
            column(name: 'value', type: 'TEXT', remarks: '配置信息')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目Id')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}