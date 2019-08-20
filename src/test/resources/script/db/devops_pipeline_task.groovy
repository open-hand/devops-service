package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_pipeline_task.groovy') {
    changeSet(author: 'scp', id: '2019-04-03-create-table') {
        createTable(tableName: "devops_pipeline_task", remarks: '阶段') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'stage_id', type: 'BIGINT UNSIGNED', remarks: '阶段Id')
            column(name: 'name', type: 'VARCHAR(50)', remarks: '名称')
            column(name: 'type', type: 'VARCHAR(50)', remarks: '类型')
            column(name: 'app_deploy_id', type: 'BIGINT UNSIGNED', remarks: '应用部署Id')
            column(name: 'is_countersigned', type: 'TINYINT UNSIGNED', remarks: '是否会签')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目ID')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(author: 'scp', id: '2019-08-11-rename-column') {
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'app_service_deploy_id', oldColumnName: 'app_deploy_id', tableName: 'devops_pipeline_task')
    }
}