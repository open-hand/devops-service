package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_deploy_app_center_host.groovy') {
    changeSet(author: 'lihao', id: '2021-08-17-create-table') {
        createTable(tableName: "devops_deploy_app_center_host", remarks: 'devops主机部署应用表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(64)', remarks: 'name') {
                constraints(nullable: false)
            }
            column(name: 'code', type: 'VARCHAR(64)', remarks: 'code') {
                constraints(nullable: false)
            }
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目id') {
                constraints(nullable: false)
            }
            column(name: 'object_id', type: 'BIGINT UNSIGNED', remarks: '部署对象id') {
                constraints(nullable: false)
            }
            column(name: 'host_id', type: 'BIGINT UNSIGNED', remarks: 'host Id') {
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

        addUniqueConstraint(tableName: 'devops_deploy_app_center_host',
                constraintName: 'uk_host_id_name', columnNames: 'host_id,name')
        addUniqueConstraint(tableName: 'devops_deploy_app_center_host',
                constraintName: 'uk_host_id_code', columnNames: 'host_id,code')

        createIndex(indexName: "idx_host_id", tableName: "devops_deploy_app_center_host") {
            column(name: "host_id")
        }
        createIndex(indexName: "idx_project_id", tableName: "devops_deploy_app_center_host") {
            column(name: "project_id")
        }
    }
}
