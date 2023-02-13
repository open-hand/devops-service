package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_config_file.groovy') {
    changeSet(author: 'wanghao', id: '2023-02-13-create-table') {
        createTable(tableName: "devops_config_file", remarks: '配置文件表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'source_type', type: 'VARCHAR(12)', remarks: '层级') {
                constraints(nullable: false)
            }
            column(name: 'source_id', type: 'BIGINT UNSIGNED', remarks: '层级Id') {
                constraints(nullable: false)
            }
            column(name: 'name', type: 'VARCHAR(255)', remarks: '配置名称') {
                constraints(nullable: false)
            }
            column(name: 'description', type: 'VARCHAR(255)', remarks: '描述')

            column(name: 'detail_id', type: 'BIGINT UNSIGNED', remarks: 'devops_config_file_detail.id') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        createIndex(indexName: "devops_config_file_n1 ", tableName: "devops_config_file") {
            column(name: "source_type")
            column(name: "source_id")
        }
        createIndex(indexName: "devops_config_file_n2 ", tableName: "devops_config_file") {
            column(name: "name")
        }
    }

}