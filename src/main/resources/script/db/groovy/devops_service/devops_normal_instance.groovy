package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_normal_instance.groovy') {
    changeSet(author: 'wanghao', id: '2021-06-30-create-table') {
        createTable(tableName: "devops_normal_instance", remarks: '普通实例') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: "host_id", type: "BIGINT UNSIGNED", remarks: '主机id') {
                constraints(nullable: false)
            }
            column(name: 'name', type: 'VARCHAR(128)', remarks: '部署包名称') {
                constraints(nullable: false)
            }
            column(name: 'instance_type', type: 'VARCHAR(32)', remarks: '实例类型') {
                constraints(nullable: false)
            }
            column(name: 'status', type: 'VARCHAR(32)', remarks: '进程状态') {
                constraints(nullable: false)
            }
            column(name: 'pid', type: 'VARCHAR(128)', remarks: '进程id')

            column(name: 'port', type: 'string(128)', remarks: '占用端口')

            column(name: 'source_type', type: 'VARCHAR(32)', remarks: '部署来源') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
    changeSet(author: 'wanghao', id: '2021-07-05-add-index') {

        createIndex(indexName: "idx_host_id", tableName: "devops_normal_instance") {
            column(name: "host_id")
        }
    }
}