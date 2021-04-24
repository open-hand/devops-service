package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_pvc.groovy') {
    changeSet(author: 'lihao', id: '2019-11-01-create-table') {
        createTable(tableName: "devops_pvc", remarks: 'k8s PVC') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，PVC id', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(40)', remarks: 'PVC名称') {
                constraints(nullable: false)
            }
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境id') {
                constraints(nullable: false)
            }
            column(name: 'pv_id', type: 'BIGINT UNSIGNED', remarks: '绑定PV id')
            column(name: 'pv_name', type: 'VARCHAR(32)', remarks: '绑定PV 名称')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目id') {
                constraints(nullable: false)
            }
            column(name: 'access_modes', type: 'VARCHAR(32)', remarks: '访问模式')
            column(name: 'request_resource', type: 'VARCHAR(32)', remarks: '申请资源大小')
            column(name: 'status', type: 'VARCHAR(32)', remarks: 'PVC状态')
            column(name: 'command_id', type: 'BIGINT UNSIGNED', remarks: '操作id')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        addUniqueConstraint(tableName: 'devops_pvc', constraintName: 'uk_devops_pvc_env_id_pvc_name', columnNames: 'env_id,name')

        createIndex(indexName: "idx_devops_pvc_name", tableName: "devops_pvc") {
            column(name: "name")
        }
    }

    changeSet(id: '2021-04-16-add-column', author: 'lihao') {
        addColumn(tableName: 'devops_pvc') {
            column(name: 'used', type: 'tinyint(1)', defaultValue: 0, remarks: '表示PVC是否被使用 0:未使用 1:已使用', afterColumn: 'status')
        }
    }
}

