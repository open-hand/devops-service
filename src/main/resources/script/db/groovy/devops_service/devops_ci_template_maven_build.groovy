package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_template_maven_build.groovy') {
    changeSet(author: 'wanghao', id: '2021-11-29-create-table-devops_ci_template_maven_publish') {
        createTable(tableName: "devops_ci_template_maven_build", remarks: 'ci模板 maven构建配置表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }

            column(name: 'repo_str', type: 'TEXT', remarks: '表单填写的Maven的依赖仓库')

            column(name: 'maven_settings', type: 'TEXT', remarks: '直接粘贴的maven的settings内容')

            column(name: 'ci_template_step_id', type: 'BIGINT UNSIGNED', remarks: '所属步骤Id') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_ci_template_maven_build', constraintName: 'uk_ci_template_step_id', columnNames: 'ci_template_step_id')
    }
    changeSet(author: 'wx', id: '2022-12-1-add-column-maven-build') {
        addColumn(tableName: 'devops_ci_template_maven_build') {
            column(name: 'nexus_maven_repo_id_str', type: 'VARCHAR(1024)', remarks: '项目下已有的maven仓库id列表', afterColumn: "repo_str")
        }
    }

}