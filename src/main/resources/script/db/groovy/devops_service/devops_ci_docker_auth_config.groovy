package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_docker_auth_config.groovy') {
    changeSet(author: 'wanghao', id: '2022-03-14-create-table') {
        createTable(tableName: "devops_ci_docker_auth_config", remarks: '流水线配置的docker认证配置') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'devops_pipeline_id', type: 'BIGINT UNSIGNED', remarks: '流水线id') {
                constraints(nullable: false)
            }
            column(name: 'domain', type: 'VARCHAR(255)', remarks: 'docker registry 域名') {
                constraints(nullable: false)
            }
            column(name: 'username', type: 'VARCHAR(255)', remarks: '用户名') {
                constraints(nullable: false)
            }
            column(name: 'password', type: 'VARCHAR(255)', remarks: '密码') {
                constraints(nullable: false)
            }
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")

        }
        addUniqueConstraint(tableName: 'devops_ci_docker_auth_config',
                constraintName: 'uk_pipeline_domain', columnNames: 'devops_pipeline_id, domain')
        createIndex(tableName: 'devops_ci_docker_auth_config', indexName: 'idx_pipeline_id') {
            column(name: 'devops_pipeline_id')
        }
    }
}