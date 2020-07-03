package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_cd_job_values.groovy') {
    changeSet(author: 'wx', id: '2020-06-30-create-table') {
        createTable(tableName: "devops_cd_job_values", remarks: 'devops_cd_job_values') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'cd_job_id', type: 'BIGINT UNSIGNED', remarks: '流水线任务job id')
            column(name: 'value', type: 'TEXT', remarks: '流水线gitlab-ci.yaml配置文件')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}