package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_deploy_group.groovy') {
    changeSet(author: 'lihao', id: '2021-08-18-create-table') {
        createTable(tableName: "devops_deploy_group", remarks: 'devops 部署组实例表') {
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

            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: 'env Id') {
                constraints(nullable: true)
            }

            column(name: 'app_config', type: 'text', remarks: '应用配置') {
                constraints(nullable: false)
            }

            column(name: 'container_config', type: 'text', remarks: '容器配置') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        addUniqueConstraint(tableName: 'devops_deploy_app_center',
                constraintName: 'uk_env_id_name', columnNames: 'env_id,name')
        addUniqueConstraint(tableName: 'devops_deploy_app_center',
                constraintName: 'uk_env_id_code', columnNames: 'env_id,code')
    }
}