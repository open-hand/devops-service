package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_maven_publish_config.groovy') {
    changeSet(author: 'wanghao', id: '2021-11-29-create-table') {
        createTable(tableName: "devops_ci_maven_publish_config", remarks: 'devops_ci_maven_publish_config') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'step_id', type: 'BIGINT UNSIGNED', remarks: '所属步骤id') {
                constraints(nullable: false)
            }
            column(name: 'nexus_maven_repo_id_str', type: 'VARCHAR(1024)', remarks: '项目下已有的maven仓库id列表')

            column(name: 'repo_str', type: 'TEXT', remarks: '表单填写的Maven的依赖仓库')

            column(name: 'maven_settings', type: 'TEXT', remarks: '直接粘贴的maven的settings内容')

            column(name: 'nexus_repo_id', type: 'BIGINT UNSIGNED', remarks: 'nexus的maven仓库在制品库的主键id')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_ci_maven_publish_config',
                constraintName: 'uk_step_id', columnNames: 'step_id')
    }

}