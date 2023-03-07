package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_host_command.groovy') {
    changeSet(author: 'wanghao', id: '2021-06-28-create-table') {
        createTable(tableName: "devops_host_command", remarks: '主机操作') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: "host_id", type: "BIGINT UNSIGNED", remarks: '主机id') {
                constraints(nullable: false)
            }
            column(name: 'instance_type', type: 'VARCHAR(32)', remarks: '实例类型') {
                constraints(nullable: false)
            }
            column(name: 'instance_id', type: 'BIGINT UNSIGNED', remarks: '实例id')

            column(name: 'command_type', type: 'VARCHAR(32)', remarks: '操作类型') {
                constraints(nullable: false)
            }

            column(name: 'cd_job_record_id', type: 'BIGINT UNSIGNED', remarks: '关联流水线任务id，为空则表示没有与流水线任务关联')

            column(name: 'status', type: 'VARCHAR(32)', remarks: '操作状态') {
                constraints(nullable: false)
            }
            column(name: 'error', type: 'VARCHAR(5000)', remarks: '错误信息')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(author: 'lihao', id: '2022-07-18-change-column') {
        modifyDataType(tableName: 'devops_host_command', columnName: 'error', newDataType: 'TEXT')
    }

    changeSet(author: 'lihao', id: '2022-11-10-add-column') {
        addColumn(tableName: 'devops_host_command') {
            column(name: 'ci_pipeline_record_id', type: 'BIGINT UNSIGNED', remarks: '流水线记录id')
        }
    }

    changeSet(author: 'lihao', id: '2023-02-03-change-column') {
        modifyDataType(tableName: 'devops_host_command', columnName: 'error', newDataType: 'MEDIUMTEXT')
    }

}