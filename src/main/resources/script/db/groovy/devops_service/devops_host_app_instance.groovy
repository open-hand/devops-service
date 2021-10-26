package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_host_app_instance.groovy') {
    changeSet(author: 'wanghao', id: '2021-09-03-create-table') {
        createTable(tableName: "devops_host_app_instance", remarks: '主机应用实例') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'app_id', type: 'BIGINT UNSIGNED', remarks: '应用id') {
                constraints(nullable: false)
            }

            column(name: 'code', type: 'VARCHAR(128)', remarks: '实例编码') {
                constraints(nullable: false)
            }

            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目id') {
                constraints(nullable: false)
            }
            column(name: "host_id", type: "BIGINT UNSIGNED", remarks: '主机id') {
                constraints(nullable: false)
            }
            column(name: 'source_type', type: 'VARCHAR(32)', remarks: '部署来源')
            column(name: 'source_config', type: 'VARCHAR(512)', remarks: '部署来源配置')

            column(name: 'pre_command', type: 'TEXT', remarks: '前置命令')
            column(name: 'run_command', type: 'TEXT', remarks: '运行命令')
            column(name: 'post_command', type: 'TEXT', remarks: '后置命令')

            column(name: 'status', type: 'VARCHAR(32)', remarks: '进程状态')
            column(name: 'pid', type: 'VARCHAR(128)', remarks: '进程id')

            column(name: 'ports', type: 'VARCHAR(128)', remarks: '占用端口')

            column(name: 'group_id', type:  'varchar(512)', remarks: 'groupId')
            column(name: 'artifact_id', type:  'varchar(512)', remarks: 'artifactId')
            column(name: 'version', type:  'varchar(512)', remarks: 'version')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        createIndex(indexName: "idx_host_id", tableName: "devops_host_app_instance") {
            column(name: "host_id")
        }
        createIndex(indexName: "idx_app_id", tableName: "devops_host_app_instance") {
            column(name: "app_id")
        }
        createIndex(indexName: "idx_project_id", tableName: "devops_host_app_instance") {
            column(name: "project_id")
        }
    }
}