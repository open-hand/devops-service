package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_chart_publish_config.groovy') {
    changeSet(author: 'wanghao', id: '2023-01-04-create-table') {
        createTable(tableName: "devops_ci_chart_publish_config", remarks: '流水线chart发布配置') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'step_id', type: 'BIGINT UNSIGNED', remarks: '所属步骤id') {
                constraints(nullable: false)
            }
            column(name: 'is_use_default_repo', type: 'TINYINT', defaultValue: "1", remarks: '是否使用默认仓库')
            column(name: 'repo_id', type: 'BIGINT UNSIGNED', remarks: 'helm仓库id')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")

        }
        addUniqueConstraint(tableName: 'devops_ci_chart_publish_config',
                constraintName: 'uk_step_id', columnNames: 'step_id')
    }
}