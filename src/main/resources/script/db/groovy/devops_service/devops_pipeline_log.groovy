package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_pipeline_log.groovy') {
    changeSet(author: 'wanghao', id: '2022-11-23-create-table') {
        createTable(tableName: "devops_pipeline_log", remarks: '流水线执行日志') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'log', type: 'TEXT', remarks: '所属流水线Id,devops_pipeline.id') {
                constraints(nullable: false)
            }
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

}
