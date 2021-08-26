package script.db.groovy.devops_service

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

    changeSet(author: 'zmf', id: '2019-01-14-add-fields-in-pod') {
        addColumn(tableName: 'devops_env_pod') {
            column(name: 'node_name', type: 'VARCHAR(255)', remarks: '节点名称', afterColumn: 'namespace')
        }
        addColumn(tableName: 'devops_env_pod') {
            column(name: 'restart_count', type: 'BIGINT UNSIGNED', remarks: 'Pod的重启次数，由Pod中的容器重启次数累加', afterColumn: 'status')
        }
    }

    changeSet(author: 'sheep', id: '2019-08-05-rename-column') {
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'instance_id', oldColumnName: 'app_instance_id', tableName: 'devops_env_pod')
    }

    changeSet(author: 'wx', id: '2020-2-18-idx-instance-id') {
        createIndex(indexName: "idx_instance_id ", tableName: "devops_env_pod") {
            column(name: "instance_id")
        }
    }
    changeSet(author: 'wanghao', id: '2021-06-10-add-column') {
        addColumn(tableName: 'devops_env_pod') {
            column(name: 'owner_ref_kind', type: 'VARCHAR(256)', remarks: '所属资源类型', afterColumn: 'resource_version')
            column(name: 'owner_ref_name', type: 'VARCHAR(256)', remarks: '所属资源名称', afterColumn: 'owner_ref_kind')
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: 'env', afterColumn: 'name')
        }
        sql("""
            UPDATE 
            devops_env_pod dep 
            SET dep.env_id = (select env_id from devops_app_service_instance dasi WHERE dasi.id = dep.instance_id)
            """)
    }
    changeSet(author: 'wanghao', id: '2021-06-10-modify-UniqueConstraint') {
        dropUniqueConstraint(constraintName: "devops_pod_uk_namespace_name", tableName: "devops_env_pod")
        addUniqueConstraint(tableName: 'devops_env_pod',
                constraintName: 'devops_pod_uk_envId_name', columnNames: 'env_id,name')
    }
}