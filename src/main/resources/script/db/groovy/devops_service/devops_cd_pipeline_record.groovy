package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_cd_pipeline_record.groovy') {
    changeSet(author: 'wanghao', id: '2020-07-02-create-table') {
        createTable(tableName: "devops_cd_pipeline_record", remarks: '流水线记录') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'pipeline_id', type: 'BIGINT UNSIGNED', remarks: '流水线Id')
            column(name: "pipeline_name", type: 'VARCHAR(64)', remarks: "pipeline name")
            column(name: 'gitlab_pipeline_id', type: 'BIGINT UNSIGNED', remarks: 'gitlab流水线记录id')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目Id')
            column(name: 'status', type: 'VARCHAR(20)', remarks: '状态')
            column(name: 'trigger_type', type: 'VARCHAR(10)', remarks: '触发方式')

            column(name: 'bpm_definition', type: 'TEXT', remarks: 'bpm定义')
            column(name: 'business_key', type: 'VARCHAR(255)', remarks: '流程实例')
            column(name: "edited", type: 'TINYINT UNSIGNED', remarks: "是否编辑", defaultValue: "0")
            column(name: 'commit_sha', type: 'VARCHAR(255)', remarks: 'commitSha')
            column(name: 'ref', type: 'VARCHAR(255)', remarks: 'ref')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(author: 'scp', id: '2020-07-02-idx-project-id') {
        createIndex(indexName: "idx_project_id ", tableName: "devops_cd_pipeline_record") {
            column(name: "project_id")
        }
    }
    changeSet(author: 'wanghao', id: '2023-03-28-drop-create-table') {
        dropTable(tableName: "devops_cd_pipeline_record")
    }

}
