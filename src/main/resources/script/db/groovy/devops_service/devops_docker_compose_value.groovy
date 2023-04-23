package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_docker_compose_value.groovy') {
    changeSet(author: 'wanghao', id: '2020-04-07-create-table') {
        createTable(tableName: "devops_docker_compose_value", remarks: 'docker compose部署时保存的yaml文件内容') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'app_id', type: 'BIGINT UNSIGNED', remarks: '应用id') {
                constraints(nullable: false)
            }
            column(name: 'remark', type: 'VARCHAR(128)', remarks: '部署备注')
            column(name: 'value', type: 'TEXT', remarks: '部署使用的docker-compose.yaml文件') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(indexName: "idx_app_id", tableName: "devops_docker_compose_value") {
            column(name: "app_id")
        }
    }
    changeSet(author: 'wanghao', id: '2023-4-18-updateDataType') {
        modifyDataType(tableName: 'devops_docker_compose_value', columnName: 'remark', newDataType: 'VARCHAR(512)')
    }

}