package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/cicd_stage_record.groovy') {
    changeSet(author: 'scp', id: '2020-06-30-create-table') {
        createTable(tableName: "cicd_stage_record", remarks: '阶段记录') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(255)', remarks: '阶段名称')
            column(name: 'cicd_stage_id', type: 'BIGINT UNSIGNED', remarks: '阶段Id')
            column(name: 'status', type: 'VARCHAR(20)', remarks: '状态')
            column(name: 'sequence', type: 'BIGINT UNSIGNED', remarks: '阶段顺序')
            column(name: 'cicd_pipeline_id', type: 'BIGINT UNSIGNED', remarks: '流水线id')
            column(name: 'type', type: 'VARCHAR(32)', remarks: 'type')

            column(name: 'cicd_pipeline_record_id', type: 'BIGINT UNSIGNED', remarks: '流水线记录Id')
            column(name: 'trigger_type', type: 'VARCHAR(10)', remarks: '触发方式')
            column(name: 'is_parallel', type: 'TINYINT UNSIGNED', remarks: '是否并行')
            column(name: 'execution_time', type: 'VARCHAR(255)', remarks: '执行时间')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目Id')
            column(name: 'audit_user', type: 'VARCHAR(255)', remarks: '审核人员')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
    changeSet(author: 'scp', id: '2019-06-30-idx-pipeline-record-id') {
        createIndex(indexName: "idx_pipeline_record_id ", tableName: "cicd_stage_record") {
            column(name: "cicd_pipeline_record_id")
        }
    }
}