package script.db.groovy.devops_service


databaseChangeLog(logicalFilePath: 'dba/devops_cluster.groovy') {
    changeSet(author: 'Younger', id: '2018-11-01-create-table') {
        createTable(tableName: "devops_cluster", remarks: 'cluster information') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'organization_id', type: 'BIGINT UNSIGNED', remarks: '组织id')
            column(name: 'name', type: 'VARCHAR(64)', remarks: '集群名字')
            column(name: 'code', type: 'VARCHAR(64)', remarks: '集群编码')
            column(name: 'description', type: 'VARCHAR(64)', remarks: '集群描述')
            column(name: 'token', type: 'VARCHAR(64)', remarks: '集群token')
            column(name: 'skip_check_project_permission', type: 'TINYINT UNSIGNED', remarks: '是否跳过项目权限校验')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        addUniqueConstraint(tableName: 'devops_cluster',
                constraintName: 'uk_orgId_code', columnNames: 'organization_id,code')
    }


    changeSet(author: 'younger', id: '2018-11-11-add-column')
            {
                addColumn(tableName: 'devops_cluster') {
                    column(name: 'choerodon_id',type: 'VARCHAR(64)',remarks:'平台标识', afterColumn: 'token')
                    column(name: 'namespaces', type: 'TEXT', remarks: '命名空间列表', afterColumn:'choerodon_id')
                }
            }

    changeSet(author: 'younger', id: '2018-11-12-add-column')
            {
                addColumn(tableName: 'devops_cluster') {
                    column(name: 'is_init',type: 'TINYINT UNSIGNED',remarks:'集群是否被初始化', afterColumn: 'skip_check_project_permission')
                }
            }

    changeSet(id: '2019-06-26-add-column', author: 'scp') {
        addColumn(tableName: 'devops_cluster') {
            column(name: 'project_id',type: 'BIGINT UNSIGNED',remarks:'项目Id', afterColumn: 'token')
        }
    }

    changeSet(id: '2019-07-09-remove-column', author: 'scp') {
        dropColumn(columnName: "project_id", tableName: "devops_cluster")
    }

    changeSet(id: '2019-07-31-add-column', author: 'scp') {
        addColumn(tableName: 'devops_cluster') {
            column(name: 'project_id',type: 'BIGINT UNSIGNED',remarks:'项目Id', afterColumn: 'token')
        }
    }

    changeSet(author: 'sheep', id: '2019-09-29-updateDataType') {
        modifyDataType(tableName: 'devops_cluster', columnName: 'description', newDataType: 'VARCHAR(500)')
    }

    changeSet(author: 'zmf', id: '2019-10-27-add-system-env-id-column') {
        addColumn(tableName: 'devops_cluster') {
            column(name: 'system_env_id', type: 'BIGINT UNSIGNED', remarks: 'cluster env id', afterColumn: 'description')
        }
    }

    changeSet(author: 'ztx', id: '2019-11-01-add-client_id-column') {
        addColumn(tableName: 'devops_cluster') {
            column(name: 'client_id', type: 'BIGINT UNSIGNED', remarks: 'client_id', afterColumn: 'system_env_id')
        }
    }

    changeSet(author: 'lihao', id: '2020-10-19-add-type-column') {
        addColumn(tableName: 'devops_cluster') {
            column(name: 'type', type: 'VARCHAR(10)', remarks: 'agent集群类型，created或者imported', afterColumn: 'id')
        }
    }

    changeSet(author: 'lihao', id: '2020-10-19-fix-type') {
        sql("UPDATE devops_cluster SET type='imported'")
    }

    changeSet(author: 'lihao', id: '2020-10-23-add-status-column') {
        addColumn(tableName: 'devops_cluster') {
            column(name: 'status', type: 'VARCHAR(32)', remarks: '集群状态', afterColumn: 'type')
        }
    }

    changeSet(author: 'lihao', id: '2020-10-30-fix-status') {
        sql("UPDATE devops_cluster SET status='disconnect' WHERE type='imported'")
    }

    changeSet(author: 'lihao', id: '2023-04-07-add-pod_name-column') {
        addColumn(tableName: 'devops_cluster') {
            column(name: 'pod_name', type: 'VARCHAR(128)', remarks: 'agent的pod名称', afterColumn: 'type')
            column(name: 'namespace', type: 'VARCHAR(128)', remarks: 'agent所在集群的命名空间', afterColumn: 'pod_name')
        }
    }
}