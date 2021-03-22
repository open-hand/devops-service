package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_pv') {
    changeSet(id: '2019-11-1-add-devops_pv', author: 'yzj') {
        createTable(tableName: "devops_pv") {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: 'id', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(40)', remarks: 'pv名称') {
                constraints(nullable: false)
            }
            column(name: 'type', type: 'VARCHAR(10)', remarks: 'pv存储类型') {
                constraints(nullable: false)
            }
            column(name: 'description', type: 'VARCHAR(40)', remarks: 'pv描述')
            column(name: 'status', 'type': 'VARCHAR(32)', remarks: 'pv状态')
            column(name: 'cluster_id', type: 'BIGINT UNSIGNED', remarks: '所属集群') {
                constraints(nullable: false)
            }
            column(name: 'storage', type: 'VARCHAR(20)', remarks: '存储容量')
            column(name: 'access_modes', type: 'VARCHAR(20)', remarks: '访问类型')
            column(name: 'skip_check_project_permission', type: 'TINYINT(1)', defaultValue: 1, remarks: '指定权限,是否只属于特定项目，默认为1')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        addUniqueConstraint(tableName: 'devops_pv',
                constraintName: 'uk_devops_pv_name_cluster', columnNames: 'name, cluster_id')
        createIndex(indexName: 'idx_devops_pv_name', tableName: 'devops_pv') {
            column(name: 'name')
        }
    }

    changeSet(id: '2019-11-06-rename-column', author: 'yzj') {
        renameColumn(columnDataType: 'VARCHAR(20)', newColumnName: 'request_resource', oldColumnName: 'storage', remarks: '存储容量', tableName: 'devops_pv')
        addColumn(tableName: 'devops_pv') {
            column(name: 'command_id', type: 'BIGINT UNSIGNED', remarks: '操作id')
        }
    }

    changeSet(id: '2019-11-11-add-column', author: 'yzj') {
        addColumn(tableName: 'devops_pv') {
            column(name: 'value_config', type: 'VARCHAR(1000)', remarks: 'pv存储类型详细配置')
        }
    }

    changeSet(id: '2019-11-28-add-column', author: 'lihao') {
        addColumn(tableName: 'devops_pv') {
            column(name: 'pvc_name', type: 'VARCHAR(40)', remarks: 'pv绑定的pvc名称')
        }
    }

    changeSet(id: '2019-11-27-add-column', author: 'lihao') {
        addColumn(tableName: 'devops_pv') {
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '所属项目id')
        }
    }

    changeSet(id: '2020-1-6-update-column', author: 'lihao') {
        sql("ALTER TABLE devops_pv MODIFY pvc_name varchar(128) null comment '绑定的PVC名称'")
    }

    changeSet(id: '2021-03-22-add-column',author: 'lihao'){
        addColumn(tableName: 'devops_pv'){
            column(name: 'labels', type: 'VARCHAR(2000)', remarks: 'pv对象的labels字段的JSON格式字符串', afterColumn: 'name')
        }
    }
}