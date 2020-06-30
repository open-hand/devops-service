package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/cicd_pipeline.groovy') {
    changeSet(author: 'scp', id: '2020-06-30-create-table') {
        createTable(tableName: "cicd_pipeline", remarks: '流水线') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(50)', remarks: '名称')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目ID')
            column(name: 'app_service_id', type: 'BIGINT UNSIGNED', remarks: '应用服务id')
            column(name: 'trigger_type', type: 'VARCHAR(10)', remarks: '触发方式')
            column(name: 'is_enabled', type: 'TINYINT UNSIGNED', remarks: '是否启用')
            column(name: 'token', type: 'CHAR(36)', remarks: '流水线token，安全性考虑')
            column(name: 'image', type: 'VARCHAR(280)', remarks: '流水线的镜像地址') {
                constraints(nullable: true)
            }
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }


    changeSet(author: 'scp', id: '2020-06-30-idx-project-id') {
        createIndex(indexName: "idx_project_id ", tableName: "cicd_pipeline") {
            column(name: "project_id")
        }
    }
}