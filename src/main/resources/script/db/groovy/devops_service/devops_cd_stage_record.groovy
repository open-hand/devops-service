package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_cd_stage_record.groovy') {
    changeSet(author: 'wanghao', id: '2020-07-02-create-table') {
        createTable(tableName: "devops_cd_stage_record", remarks: 'CD阶段记录') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'pipeline_record_id', type: 'BIGINT UNSIGNED', remarks: '流水线记录Id')
            column(name: 'stage_id', type: 'BIGINT UNSIGNED', remarks: '阶段Id')
            column(name: 'stage_name', type: 'VARCHAR(50)', remarks: '阶段名称')
            column(name: 'status', type: 'VARCHAR(20)', remarks: '状态')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目Id')
            column(name: 'sequence', type: 'BIGINT UNSIGNED', remarks: '阶段顺序') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(author: 'wanghao', id: '2020-07-02-idx-pipeline-record-id') {
        createIndex(indexName: "idx_pipeline_record_id ", tableName: "devops_cd_stage_record") {
            column(name: "pipeline_record_id")
        }
    }
    changeSet(author: 'wanghao', id: '2023-03-28-drop-create-table') {
        dropTable(tableName: "devops_cd_stage_record")
    }
}