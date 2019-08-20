package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_service.groovy') {
    changeSet(author: 'Zenger', id: '2018-04-13-create-table') {
        createTable(tableName: "devops_service", remarks: '网络') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境 ID')
            column(name: 'app_id', type: 'BIGINT UNSIGNED', remarks: '应用 ID')
            column(name: 'name', type: 'VARCHAR(253)', remarks: '网络名称')
            column(name: 'namespace', type: 'VARCHAR(128)', remarks: '命名空间')
            column(name: 'status', type: 'VARCHAR(16)', remarks: '状态')
            column(name: 'port', type: 'BIGINT UNSIGNED', remarks: '网络端口')
            column(name: 'target_port', type: 'BIGINT UNSIGNED', remarks: '网络映射端口')
            column(name: 'external_ip', type: 'VARCHAR(32)', remarks: '外部IP')
            column(name: 'label', type: 'VARCHAR(1000)', remarks: '网络标签')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        createIndex(indexName: "devops_service_idx_name", tableName: "devops_service") {
            column(name: "name")
        }
        addUniqueConstraint(tableName: 'devops_service', constraintName: 'uk_namespace_name',
                columnNames: 'namespace,name')
    }

    changeSet(author: 'runge', id: '2018-07-31-change-column') {
        renameColumn(tableName: 'devops_service', columnDataType: 'VARCHAR(1000)',
                oldColumnName: 'label', newColumnName: 'annotations', remarks: '网络注释')
        modifyDataType(tableName: 'devops_service', columnName: 'external_ip', newDataType: 'VARCHAR(1000)')
        addColumn(tableName: 'devops_service') {
            column(name: 'ports', type: 'VARCHAR(1000)', remarks: '网络端口', afterColumn: 'status')
            column(name: 'labels', type: 'VARCHAR(1000)', remarks: '网络标签', afterColumn: 'external_ip')
        }
    }

    changeSet(author: 'runge', id: '2018-08-08-add-column') {
        addColumn(tableName: 'devops_service') {
            column(name: 'type', type: 'VARCHAR(30)', remarks: '网络类型', afterColumn: 'status')
        }
    }

    changeSet(author: 'runge', id: '2018-08-14-move-data') {
        update(tableName: 'devops_service') {
            column(name: 'ports', type: 'VARCHAR(1000)',
                    valueComputed: 'CONCAT( \'[{"name":"http","protocol":"TCP","port":\', `port`, \',"targetPort":\', target_port, \'}]\' )')
            where('ports IS NULL')
        }
        update(tableName: 'devops_service') {
            column(name: 'type', type: 'VARCHAR(30)', value: 'ClusterIP')
            where('type IS NULL')
        }
        dropColumn(columnName: "port", tableName: "devops_service")
        dropColumn(columnName: "target_port", tableName: "devops_service")

    }

    changeSet(author: 'younger', id: '2018-09-03-modify-UniqueConstraint') {
        dropUniqueConstraint(constraintName: "uk_namespace_name", tableName: "devops_service")
        addUniqueConstraint(tableName: 'devops_service',
                constraintName: 'devops_service_uk_namespace_name', columnNames: 'namespace,name')
    }


    changeSet(author: 'younger', id: '2018-09-10-add-column')
            {
                addColumn(tableName: 'devops_service') {
                    column(name: 'command_id', type: 'BIGINT UNSIGNED', remarks: 'command id', afterColumn: 'env_id')
                }
            }


    changeSet(id: '2018-10-08-drop-column', author: 'younger') {
        dropUniqueConstraint(constraintName: "devops_service_uk_namespace_name", tableName: "devops_service")
        dropColumn(columnName: "namespace", tableName: "devops_service")

    }


    changeSet(author: 'younger', id: '2018-12-19-add-column')
            {
                addColumn(tableName: 'devops_service') {
                    column(name: 'end_points', type: 'VARCHAR(1000)', remarks: 'endpoint的ip和端口', afterColumn: 'ports')
                }
            }

    changeSet(author: 'younger', id: '2019-01-04-add-column')
            {
                addColumn(tableName: 'devops_service') {
                    column(name: 'load_balance_ip', type: 'VARCHAR(32)', remarks: 'load balance类型的ip', afterColumn: 'end_points')
                }
            }

    changeSet(author: 'scp', id: '2019-07-29-rename-column') {
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'app_service_id', oldColumnName: 'app_id', tableName: 'devops_service')
    }
}