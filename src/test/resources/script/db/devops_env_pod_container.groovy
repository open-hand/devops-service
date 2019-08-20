package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_env_pod_container.groovy') {
    changeSet(author: 'Runge', id: '2018-05-16-create-table') {
        createTable(tableName: "devops_env_pod_container", remarks: 'pod container') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'pod_id', type: 'BIGINT UNSIGNED', remarks: 'pod ID')
            column(name: 'container_name', type: 'VARCHAR(64)', remarks: 'container name')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        createIndex(indexName: "idx_pod_id", tableName: "devops_env_pod_container") {
            column(name: "pod_id")
        }
    }

    changeSet(author: 'Sheep', id: '2019-06-10-delete-table') {
        dropTable(tableName: "devops_env_pod_container")
    }
}