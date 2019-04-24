package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_pipeline_record.groovy') {
    changeSet(author: 'scp', id: '2019-04-03-create-table') {
        createTable(tableName: "devops_pipeline_record", remarks: '流水线记录') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'pipeline_id', type: 'BIGINT UNSIGNED', remarks: '流水线Id')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目Id')
            column(name: 'status', type: 'VARCHAR(20)', remarks: '状态')
            column(name: 'trigger_type', type: 'VARCHAR(10)', remarks: '触发方式')
            column(name: 'execution_time', type: 'DATETIME', remarks: '执行时间')
            column(name: 'bpm_definition', type: 'TEXT', remarks: 'bpm定义')
            column(name: 'process_instance_id', type: 'VARCHAR(255)', remarks: '流程实例Id')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
    changeSet(author: 'scp', id: '2019-04-18-devops_pipeline_record-drop-column') {
        dropColumn(columnName: "execution_time", tableName: "devops_pipeline_record")
    }
    changeSet(author: 'sheep', id: '2019-04-24-devops_pipeline_record-add-column') {
        dropColumn(columnName: "pipeline_name", tableName: "devops_pipeline_record")
    }
}