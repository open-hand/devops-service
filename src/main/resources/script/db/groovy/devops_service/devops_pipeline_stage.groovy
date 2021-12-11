package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_pipeline_stage.groovy') {
    changeSet(author: 'scp', id: '2019-04-03-create-table') {
        createTable(tableName: "devops_pipeline_stage", remarks: '阶段') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'stage_name', type: 'VARCHAR(50)', remarks: '名称')
            column(name: 'trigger_type', type: 'VARCHAR(10)', remarks: '触发方式')
            column(name: 'is_parallel', type: 'TINYINT UNSIGNED', remarks: '是否并行')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目ID')
            column(name: 'pipeline_id', type: 'BIGINT UNSIGNED', remarks: '工作台Id')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(author: 'lihao', id: '2020-07-19-add-index') {
        createIndex(tableName: "devops_pipeline_stage", indexName: "devops_pipeline_stage_pipeline_id_idx") {
            column(name: "pipeline_id")
        }
    }
    changeSet(author: 'wanghao', id: '2021-12-11-delete-table') {
        dropTable(tableName: "devops_pipeline_stage")
    }
}