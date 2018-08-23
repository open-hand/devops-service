package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_ingress_path.groovy') {
    changeSet(author: 'Runge', id: '2018-04-19-create-table') {
        createTable(tableName: "devops_ingress_path", remarks: '域名路径') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'ingress_id', type: 'BIGINT UNSIGNED', remarks: '域名')
            column(name: 'path', type: 'VARCHAR(64)', remarks: '路径')
            column(name: 'service_id', type: 'BIGINT UNSIGNED', remarks: '网络ID')
            column(name: 'service_name', type: 'VARCHAR(253)', remarks: '网络名')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(author: 'Runge', id: '2018-08-23-add-column') {
        addColumn(tableName: 'devops_ingress_path') {
            column(name: 'service_port', type: 'BIGINT UNSIGNED', remarks: '网络端口')
        }
    }
}