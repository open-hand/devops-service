package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_auto_deploy_record.groovy') {
    changeSet(author: 'scp', id: '2019-02-26-create-table') {
        createTable(tableName: "devops_auto_deploy_record", remarks: '记录ID') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }

            column(name: 'task_name', type: 'VARCHAR(50)', remarks: '任务名称') {
                constraints(nullable: false)
            }

            column(name: 'auto_deploy_id', type: 'BIGINT UNSIGNED', remarks: '自动部署') {
                constraints(nullable: false)
            }
            column(name: 'instance_id', type: 'BIGINT UNSIGNED', remarks: '任务名称')

            column(name: 'version_id', type: 'BIGINT UNSIGNED', remarks: '应用ID') {
                constraints(nullable: false)
            }
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境ID') {
                constraints(nullable: false)
            }

            column(name: 'app_id', type: 'BIGINT UNSIGNED', remarks: '应用ID') {
                constraints(nullable: false)
            }
            column(name: 'status', type: 'VARCHAR(32)', remarks: '实例状态') {
                constraints(nullable: false)
            }
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目ID') {
                constraints(nullable: false)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        createIndex(indexName: "idx_app_id_env_name", tableName: "devops_auto_deploy_record") {
            column(name: "app_id")
            column(name: 'env_id')
            column(name: 'task_name')
        }

    }
}
