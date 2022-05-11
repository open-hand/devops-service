package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_pipeline_schedule.groovy') {
    changeSet(author: 'wanghao', id: '2022-03-24-create-table') {
        createTable(tableName: "devops_ci_pipeline_schedule", remarks: 'ci流水线定时配置表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'app_service_id', type: 'BIGINT UNSIGNED', remarks: '应用服务id') {
                constraints(nullable: false)
            }
            column(name: 'name', type: 'VARCHAR(60)', remarks: '定时任务名称') {
                constraints(nullable: false)
            }
            column(name: 'pipeline_schedule_id', type: 'BIGINT UNSIGNED', remarks: 'gitlab pipeline_schedule_id') {
                constraints(nullable: false)
            }
            column(name: 'ref', type: 'VARCHAR(255)', remarks: '触发分支') {
                constraints(nullable: false)
            }

            column(name: 'trigger_type', type: 'VARCHAR(255)', remarks: '触发类型：周期触发，单次触发') {
                constraints(nullable: false)
            }

            column(name: 'week_number', type: 'VARCHAR(255)', remarks: '每周几触发') {
                constraints(nullable: false)
            }

            column(name: 'start_hour_of_day', type: 'BIGINT UNSIGNED', remarks: '开始时间：周期触发时需要，0-23')
            column(name: 'end_hour_of_day', type: 'BIGINT UNSIGNED', remarks: '结束时间：周期触发时需要，0-23')
            column(name: 'period', type: 'BIGINT UNSIGNED', remarks: '执行间隔：周期触发时需要，10，20，30，40，50，60，120，240')

            column(name: 'execute_time', type: 'VARCHAR(255)', remarks: '执行时间：单次触发时需要,HH:mm')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
        addUniqueConstraint(tableName: 'devops_ci_pipeline_schedule',
                constraintName: 'uk_name', columnNames: 'app_service_id,name')

        createIndex(indexName: "idx_app_service_id", tableName: "devops_ci_pipeline_schedule") {
            column(name: "app_service_id")
        }
    }
}
