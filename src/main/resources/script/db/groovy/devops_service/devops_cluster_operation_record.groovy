package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_cluster_operation_record.groovy') {
    changeSet(author: 'lihao', id: '2020-10-27-create-table') {
        createTable(tableName: 'devops_cluster_operation_record', remarks: '集群和节点操作记录表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，id', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'type', type: 'VARCHAR(10)', remarks: '操作类型') {
                constraints(nullable: false)
            }
            column(name: 'cluster_id', type: 'BIGINT UNSIGNED', remarks: '集群id') {
                constraints(nullable: false)
            }
            column(name: 'node_id', type: 'BIGINT UNSIGNED', remarks: '节点id') {
                constraints(nullable: false)
            }
            column(name: 'status', type: 'VARCHAR(10)', remarks: '操作状态')
            column(name: 'error_msg', type: 'TEXT', remarks: '错误信息')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        createIndex(indexName: "idx_cluster_id_and_node_id", tableName: "devops_cluster_operation_record") {
            column(name: "cluster_id")
            column(name: "node_id")
        }
    }
}