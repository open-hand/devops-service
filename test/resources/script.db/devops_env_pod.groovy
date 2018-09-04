package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_env_pod.groovy') {
    changeSet(author: 'Zenger', id: '2018-04-12-create-table') {
        createTable(tableName: "devops_env_pod", remarks: '应用容器') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'app_instance_id', type: 'BIGINT UNSIGNED', remarks: '实例 ID')
            column(name: 'name', type: 'VARCHAR(64)', remarks: '容器名称')
            column(name: 'namespace', type: 'VARCHAR(128)', remarks: '命名空间')
            column(name: 'ip', type: 'VARCHAR(64)', remarks: '容器地址')
            column(name: 'status', type: 'VARCHAR(32)', remarks: '容器状态')
            column(name: 'is_ready', type: 'TINYINT UNSIGNED', remarks: '是否可用')
            column(name: 'resource_version', type: 'VARCHAR(32)', remarks: 'pod 版本记录')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        addUniqueConstraint(tableName: 'devops_env_pod', constraintName: 'uk_namespace_name', columnNames: 'namespace,name')
        createIndex(indexName: "idx_resource_version", tableName: "devops_env_pod") {
            column(name: "resource_version")
        }
    }

    changeSet(author: 'younger', id: '2018-09-03-modify-UniqueConstraint') {
        dropUniqueConstraint(constraintName: "uk_namespace_name",tableName: "devops_env_pod")
        addUniqueConstraint(tableName: 'devops_env_pod',
                constraintName: 'devops_pod_uk_namespace_name', columnNames: 'namespace,name')
    }
}