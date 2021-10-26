package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ingress.groovy') {
    changeSet(author: 'Runge', id: '2018-04-19-create-table') {
        createTable(tableName: "devops_ingress", remarks: '域名管理') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目ID')
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境 ID')
            column(name: 'name', type: 'VARCHAR(253)', remarks: '域名名称')
            column(name: 'domain', type: 'VARCHAR(253)', remarks: '域名地址')
            column(name: 'is_usable', type: 'TINYINT UNSIGNED', remarks: '是否可用', defaultValueBoolean: 'false')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(author: 'Runge', id: '2018-06-05-add-column') {
        addColumn(tableName: 'devops_ingress') {
            column(name: 'status', type: 'VARCHAR(10)', remarks: '状态')
        }
    }

    changeSet(author: 'Runge', id: '2018-08-29-add-column') {
        addColumn(tableName: 'devops_ingress') {
            column(name: 'cert_id', type: 'BIGINT UNSIGNED', remarks: '证书ID', afterColumn: 'env_id')
        }
    }


    changeSet(author: 'younger', id: '2018-09-10-add-column')
            {
                addColumn(tableName: 'devops_ingress') {
                    column(name: 'command_id', type: 'BIGINT UNSIGNED', remarks: 'command id', afterColumn: 'env_id')
                }
            }

    changeSet(author: 'zmf', id: '2020-03-17-add-column-annotations') {
        addColumn(tableName: 'devops_ingress') {
            column(name: 'annotations', type: 'VARCHAR(2000)', remarks: 'ingress对象的Annotations字段的JSON格式字符串', afterColumn: 'domain')
        }
    }

    changeSet(author: 'lihao', id: '2021-06-15-add-column-instance_id') {
        addColumn(tableName: 'devops_ingress') {
            column(name: 'instance_id', type: 'BIGINT UNSIGNED', remarks: 'ingress对象关联的实例id', afterColumn: 'command_id')
        }
    }
}