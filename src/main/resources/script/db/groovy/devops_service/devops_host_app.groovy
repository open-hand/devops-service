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

    changeSet(author: 'lihao', id: '2021-11-02-drop-index') {
        dropIndex(indexName: "idx_host_id", tableName: "devops_host_app")
    }

    changeSet(author: 'wx', id: '2022-02-24-drop-index') {
        dropIndex(indexName: "uk_host_id_name", tableName: "devops_host_app")
        dropIndex(indexName: "uk_host_id_code", tableName: "devops_host_app")
    }
    changeSet(author: 'wanghao', id: '2022-04-07-add-index') {
        addUniqueConstraint(tableName: 'devops_host_app',
                constraintName: 'uk_host_id_name', columnNames: 'project_id,name')
        addUniqueConstraint(tableName: 'devops_host_app',
                constraintName: 'uk_host_id_code', columnNames: 'project_id,code')
    }

    changeSet(author: 'wanghao', id: '2022-04-07-add-column') {
        addColumn(tableName: 'devops_host_app') {
            column(name: 'run_command', type: 'TEXT', remarks: '运行命令, docker_compose应用才需要') {
                constraints(nullable: true)
            }
            column(name: 'effect_value_id', type: 'BIGINT UNSIGNED', remarks: '当前使用的配置id, docker_compose应用才需要') {
                constraints(nullable: true)
            }
        }
    }
    changeSet(author: 'wanghao', id: '2023-01-16-drop-index') {
        dropIndex(indexName: "uk_host_id_name", tableName: "devops_host_app")
        dropIndex(indexName: "uk_host_id_code", tableName: "devops_host_app")
        dropIndex(indexName: "uk_host_id_name", tableName: "devops_host_app")
    }

    changeSet(author: 'wanghao', id: '2023-01-16-add-index') {

        addUniqueConstraint(tableName: 'devops_host_app',
                constraintName: 'devops_host_app_u1', columnNames: 'host_id,name')
        addUniqueConstraint(tableName: 'devops_host_app',
                constraintName: 'devops_host_app_u2', columnNames: 'host_id,code')
    }

}