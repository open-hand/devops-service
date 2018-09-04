package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_env_group.groovy') {
    changeSet(author: 'younger', id: '2018-09-04-create-table') {
        createTable(tableName: "devops_env_group", remarks: '环境组管理') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，环境ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目 ID')
            column(name: 'name', type: 'VARCHAR(32)', remarks: '环境组名称')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_env_group',
                constraintName: 'devops_env_group_uk_project_id_code', columnNames: 'project_id,name')
    }
}