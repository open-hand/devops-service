package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_auto_deploy.groovy') {
    changeSet(author: 'scp', id: '2019-02-25-create-table') {
        createTable(tableName: "devops_auto_deploy", remarks: '自动部署') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }

            column(name: 'task_name', type: 'VARCHAR(50)', remarks: '任务名称') {
                constraints(nullable: false)
            }
            column(name: 'app_id', type: 'BIGINT UNSIGNED', remarks: '应用ID') {
                constraints(nullable: false)
            }
            column(name: 'trigger_version', type: 'VARCHAR(200)', remarks: '触发版本')

            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境ID') {
                constraints(nullable: false)
            }

            column(name: 'value_id', type: 'BIGINT UNSIGNED', remarks: 'Value ID') {
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

        createIndex(indexName: "idx_task_name", tableName: "devops_auto_deploy") {
            column(name: 'task_name')
        }

        addUniqueConstraint(tableName: 'devops_auto_deploy',
                constraintName: 'uk_task_name', columnNames: 'project_id,task_name')
    }

    changeSet(author: 'scp', id: '2019-02-28-add-column') {
        addColumn(tableName: 'devops_auto_deploy') {
            column(name: 'instance_id', type: 'BIGINT UNSIGNED', remarks: '实例ID')
            column(name: 'is_enabled', type: 'TINYINT UNSIGNED', remarks: '是否启用', defaultValue: "1")
        }
    }

    changeSet(author: 'scp', id: '2019-03-01-add-column') {
        addColumn(tableName: 'devops_auto_deploy') {
            column(name: 'instance_name', type: 'VARCHAR(100)', remarks: '实例名称')
        }
    }
}
