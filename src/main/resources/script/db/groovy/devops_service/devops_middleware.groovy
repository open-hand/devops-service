package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_middleware.groovy') {
    changeSet(author: 'lihao', id: '2021-03-15-create-table') {
        createTable(tableName: "devops_middleware", remarks: '中间件') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '中间件所属项目')
            column(name: 'name', type: 'VARCHAR(64)', remarks: '中间件名称')
            column(name: 'type', type: 'VARCHAR(32)', remarks: '中间件类型，比如Redis、MySQL')
            column(name: 'version', type: 'VARCHAR(64)', remarks: '部署的版本')
            column(name: 'mode', type: 'VARCHAR(10)', remarks: '中间件部署模式')
            column(name: 'host_ids', type: 'VARCHAR(320)', remarks: '选择的主机，逗号分隔的id')
            column(name: 'configuration', type: 'VARCHAR(2048)', remarks: '配置内容')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        addUniqueConstraint(tableName: 'devops_middleware', constraintName: 'uk_project_id_name', columnNames: 'project_id,name')
    }

    changeSet(author: 'lihao', id: '2021-04-20-update-column') {
        modifyDataType(tableName: 'devops_middleware', columnName: 'mode', newDataType: 'VARCHAR(32)')
    }
}
