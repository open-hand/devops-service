package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_app_external_config.groovy') {
    changeSet(author: 'wanghao', id: '2021-09-28-create-table') {
        createTable(tableName: "devops_app_external_config", remarks: 'devops外部应用配置表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'app_service_id', type: 'BIGINT UNSIGNED', remarks: '应用服务id') {
                constraints(nullable: false)
            }
            column(name: 'repository_url', type: 'VARCHAR(512)', remarks: '外部仓库地址') {
                constraints(nullable: false)
            }
            column(name: 'auth_type', type: 'VARCHAR(64)', remarks: '认证类型：用户名密码：username_password,Token: access_token') {
                constraints(nullable: false)
            }
            column(name: 'access_token', type: 'VARCHAR(32)', remarks: '用户gitlab access_token')
            column(name: 'username', type: 'VARCHAR(256)', remarks: 'gitlab用户名')
            column(name: 'password', type: 'VARCHAR(256)', remarks: 'gitlab密码')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        addUniqueConstraint(tableName: 'devops_app_external_config',
                constraintName: 'uk_repository_url', columnNames: 'repository_url')
        addUniqueConstraint(tableName: 'devops_app_external_config',
                constraintName: 'uk_app_service_id', columnNames: 'app_service_id')
    }
}