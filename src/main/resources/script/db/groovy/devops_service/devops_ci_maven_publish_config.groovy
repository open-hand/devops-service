package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_maven_publish_config.groovy') {
    changeSet(author: 'wanghao', id: '2021-11-29-create-table') {
        createTable(tableName: "devops_ci_maven_publish_config", remarks: 'CI maven发布配置信息表') {
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

    changeSet(author: 'wanghao', id: '2022-02-23-add-column') {
        addColumn(tableName: 'devops_ci_maven_publish_config') {
            column(name: "target_repo_str", type: "TEXT", remarks: '发包的目的仓库信息 json格式', afterColumn: "repo_str")
        }
    }

    changeSet(author: 'wanghao', id: '2023-02-07-add-column') {
        addColumn(tableName: 'devops_ci_maven_publish_config') {
            column(name: "gav_source_type", type: "VARCHAR(32)", remarks: '坐标来源类型：pom、custom', defaultValue: "pom", afterColumn: "nexus_repo_id")
            column(name: "pom_location", type: "VARCHAR(256)", remarks: 'pom文件路径', defaultValue: "pom.xml", afterColumn: "gav_source_type")
            column(name: "group_id", type: "VARCHAR(256)", remarks: 'groupId', afterColumn: "pom_location")
            column(name: "artifact_id", type: "VARCHAR(256)", remarks: 'artifactId', afterColumn: "groupId")
            column(name: "version", type: "VARCHAR(256)", remarks: 'version', afterColumn: "artifactId")
            column(name: "packaging", type: "VARCHAR(256)", remarks: 'packaging', defaultValue: "jar", afterColumn: "version")
        }
    }

}