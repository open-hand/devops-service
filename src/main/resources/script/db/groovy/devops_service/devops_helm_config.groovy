package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_helm_config.groovy') {
    changeSet(author: 'lihao', id: '2022-07-12-create-table') {
        createTable(tableName: "devops_helm_config", remarks: 'helm仓库配置表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(60)', remarks: '仓库名称') {
                constraints(nullable: false)
            }
            column(name: 'url', type: 'text', remarks: 'helm仓库地址 平台层或组织层为仓库地址前缀部分 项目层是完整的仓库地址') {
                constraints(nullable: false)
            }

            column(name: 'username', type: 'VARCHAR(64)', remarks: '用户名')
            column(name: 'password', type: 'VARCHAR(128)', remarks: '密码')
            column(name: 'resource_type', type: 'VARCHAR(16)', remarks: '关联该仓库配置的层级 project/organization/site') {
                constraints(nullable: false)
            }
            column(name: 'resource_id', type: 'BIGINT UNSIGNED', remarks: '关联该仓库配置的资源id, 项目id 组织id 平台层为0') {
                constraints(nullable: false)
            }
            column(name: "deleted",type: 'TINYINT(1)',remarks: '软删除 0未删除 1已删除',defaultValue: 0)
            column(name: "repo_private", type: 'TINYINT(1)', remarks: '是否私有 0 否 1是', defaultValue: 0)
            column(name: 'repo_default', type: 'TINYINT(1)', remarks: '是否为默认仓库', defaultValue: 0)

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        addUniqueConstraint(tableName: 'devops_helm_config', constraintName: 'devops_helm_config_n1', columnNames: 'resource_type,resource_id,name')

        createIndex(indexName: "devops_helm_config_n1", tableName: "devops_helm_config") {
            column(name: "resource_id")
            column(name: "resource_type")
        }
    }
}