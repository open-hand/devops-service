package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_pipeline.groovy') {
    changeSet(author: 'scp', id: '2019-04-03-create-table') {
        createTable(tableName: "devops_pipeline", remarks: '流水线') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(50)', remarks: '名称')
            column(name: 'trigger_type', type: 'VARCHAR(10)', remarks: '触发方式')
            column(name: 'is_enabled', type: 'TINYINT UNSIGNED', remarks: '是否启用')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目ID')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
    changeSet(author: 'scp', id: '2019-04-15-devops_pipeline-drop-column') {
        dropColumn(columnName: "is_enabled", tableName: "devops_pipeline")
        addColumn(tableName: 'devops_pipeline') {
            column(name: 'is_enabled', type: 'TINYINT UNSIGNED', remarks: '是否启用',defaultValue: "1")
        }
    }

    changeSet(author: 'scp', id: '2019-06-04-idx-project-id') {
        createIndex(indexName: "devops_pipeline_idx_project_id ", tableName: "devops_pipeline") {
            column(name: "project_id")
        }
    }
}