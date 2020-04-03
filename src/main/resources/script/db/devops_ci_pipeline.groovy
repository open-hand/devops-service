package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_ci_pipeline.groovy') {
    changeSet(author: 'wanghao', id: '2020-04-02-create-table') {
        createTable(tableName: "devops_ci_pipeline", remarks: 'devops_ci_pipeline') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(255)', remarks: '流水线名称')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目id')
            column(name: 'app_service_id', type: 'BIGINT UNSIGNED', remarks: '应用服务id')
            column(name: 'trigger_type', type: 'VARCHAR(255)', remarks: '触发方式:auto, 自动触发，manual')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_ci_pipeline',
                constraintName: 'uk_app_service_id', columnNames: 'app_service_id')
    }
}
