package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_npm_publish_config.groovy') {
    changeSet(author: 'wanghao', id: '2023-01-04-create-table') {
        createTable(tableName: "devops_ci_npm_publish_config", remarks: '流水线npm发布配置') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'step_id', type: 'BIGINT UNSIGNED', remarks: '所属步骤id') {
                constraints(nullable: false)
            }
            column(name: 'nexus_repo_id', type: 'BIGINT UNSIGNED', remarks: 'nexus的maven仓库在制品库的主键id')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")

        }
        addUniqueConstraint(tableName: 'devops_ci_npm_publish_config',
                constraintName: 'devops_ci_npm_publish_config_u1', columnNames: 'step_id')
    }
}