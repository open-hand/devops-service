package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_template_maven_publish.groovy') {
    changeSet(author: 'wanghao', id: '2021-11-29-create-table-devops_ci_template_maven_publish') {
        createTable(tableName: "devops_ci_template_maven_publish", remarks: 'ci模板 maven发布配置表') {
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
        addUniqueConstraint(tableName: 'devops_ci_template_maven_publish', constraintName: 'uk_ci_template_step_id', columnNames: 'ci_template_step_id')
    }

    changeSet(author: 'wx', id: '2022-12-1-add-column') {
        addColumn(tableName: 'devops_ci_template_maven_publish') {
            column(name: 'nexus_maven_repo_id_str', type: 'VARCHAR(1024)', remarks: '项目下已有的maven仓库id列表',afterColumn: "repo_str")
            column(name: 'nexus_repo_id', type: 'BIGINT UNSIGNED', remarks: 'nexus的maven仓库在制品库的主键id',afterColumn: "repo_str")
            column(name: "target_repo_str", type: "TEXT", remarks: '发包的目的仓库信息 json格式', afterColumn: "repo_str")
        }
    }

}