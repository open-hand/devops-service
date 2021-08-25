package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_host_app.groovy') {
    changeSet(author: 'wanghao', id: '2021-08-20-create-table') {
        createTable(tableName: "devops_host_app", remarks: '主机应用') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目id') {
                constraints(nullable: false)
            }
            column(name: "host_id", type: "BIGINT UNSIGNED", remarks: '主机id') {
                constraints(nullable: false)
            }
            column(name: 'name', type: 'VARCHAR(128)', remarks: '应用名称') {
                constraints(nullable: false)
            }
            column(name: 'code', type: 'VARCHAR(64)', remarks: 'code') {
                constraints(nullable: false)
            }
            column(name: 'operation_type', type: 'VARCHAR(32)', remarks: '操作类型') {
                constraints(nullable: false)
            }
            column(name: 'rdupm_type', type: 'VARCHAR(32)', remarks: '制品类型 jar') {
                constraints(nullable: false)
            }
            column(name: 'source_type', type: 'VARCHAR(32)', remarks: '部署来源') {
                constraints(nullable: false)
            }
            column(name: 'source_config', type: 'VARCHAR(512)', remarks: '部署来源配置') {
                constraints(nullable: false)
            }

            column(name: 'value', type: 'VARCHAR(512)', remarks: '部署命令') {
                constraints(nullable: false)
            }

            column(name: 'status', type: 'VARCHAR(32)', remarks: '进程状态')
            column(name: 'pid', type: 'VARCHAR(128)', remarks: '进程id')

            column(name: 'ports', type: 'VARCHAR(128)', remarks: '占用端口')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_host_app',
                constraintName: 'uk_host_id_name', columnNames: 'host_id,name')
        addUniqueConstraint(tableName: 'devops_host_app',
                constraintName: 'uk_host_id_code', columnNames: 'host_id,code')

        createIndex(indexName: "idx_host_id", tableName: "devops_host_app") {
            column(name: "host_id")
        }
        createIndex(indexName: "idx_project_id", tableName: "devops_host_app") {
            column(name: "project_id")
        }
    }

    changeSet(author: 'wanghao', id: '2021-08-24-add-column') {
        addColumn(tableName: 'devops_app_service') {
            column(name: 'group_id', type:  'varchar(512)', remarks: 'groupId', afterColumn: 'ports')
            column(name: 'artifact_id', type:  'varchar(512)', remarks: 'artifactId', afterColumn: 'group_id')
            column(name: 'version', type:  'varchar(512)', remarks: 'version', afterColumn: 'artifact_id')
        }
    }
}