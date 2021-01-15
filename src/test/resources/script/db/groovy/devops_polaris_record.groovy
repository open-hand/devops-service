package script.db.groovy

databaseChangeLog(logicalFilePath: 'dba/devops_polaris_record.groovy') {
    changeSet(author: 'zmf', id: '2020-02-14-create-table-polaris-record') {
        createTable(tableName: "devops_polaris_record", remarks: 'polaris扫描纪录') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'scope_id', type: 'BIGINT UNSIGNED', remarks: '纪录对象id(集群id或环境id)') {
                constraints(nullable: false)
            }
            column(name: 'scope', type: 'VARCHAR(45)', remarks: '纪录对象类型(cluster/env)') {
                constraints(nullable: false)
            }
            column(name: 'status', type: 'VARCHAR(45)', remarks: '操作状态') {
                constraints(nullable: false)
            }
            column(name: "scan_date_time", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP", remarks: "这次扫描开始时间") {
                constraints(nullable: false)
            }
            column(name: "last_scan_date_time", type: "DATETIME", remarks: "上次扫描结束时间")
            column(name: 'successes', type: 'BIGINT UNSIGNED', remarks: '通过的检测项数量')
            column(name: 'warnings', type: 'BIGINT UNSIGNED', remarks: '警告的检测项数量')
            column(name: 'errors', type: 'BIGINT UNSIGNED', remarks: '错误的检测项数量')
            column(name: 'score', type: 'BIGINT UNSIGNED', remarks: '扫描结果的得分')
            column(name: 'kubernetes_version', type: 'VARCHAR(45)', remarks: '扫描出的集群版本')
            column(name: 'pods', type: 'BIGINT UNSIGNED', remarks: 'pod数量')
            column(name: 'namespaces', type: 'BIGINT UNSIGNED', remarks: 'namespace数量')
            column(name: 'nodes', type: 'BIGINT UNSIGNED', remarks: '节点数量')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        addUniqueConstraint(tableName: 'devops_polaris_record',
                constraintName: 'polaris_record_uk_scope_id_scope', columnNames: 'scope_id, scope')
    }
}