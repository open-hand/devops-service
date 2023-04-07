package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_cd_job_record.groovy') {
    changeSet(author: 'wanghao', id: '2020-07-02-create-table') {
        createTable(tableName: "devops_cd_job_record", remarks: 'CD任务记录') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'stage_record_id', type: 'BIGINT UNSIGNED', remarks: '阶段记录Id')
            column(name: 'job_id', type: 'BIGINT UNSIGNED', remarks: '任务Id')
            column(name: 'name', type: 'VARCHAR(50)', remarks: '任务名称')
            column(name: 'type', type: 'VARCHAR(20)', remarks: '任务类型')
            column(name: 'status', type: 'VARCHAR(50)', remarks: '状态')
            column(name: 'trigger_type', type: 'VARCHAR(255)', remarks: '触发方式', defaultValue: 'refs')
            column(name: 'trigger_value', type: 'VARCHAR(255)', remarks: '触发方式对应的值')

            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目ID')
            column(name: 'metadata', type: 'TEXT', remarks: 'job详细信息，定义了job执行内容')

            column(name: "started_date", type: "DATETIME", remarks: 'job开始执行时间')
            column(name: "finished_date", type: "DATETIME", remarks: 'job结束时间')
            column(name: "duration_seconds", type: "BIGINT UNSIGNED", remarks: 'job执行时长')
            column(name: 'sequence', type: 'BIGINT UNSIGNED', remarks: '任务顺序') {
                constraints(nullable: false)
            }
            column(name: 'deploy_info_id', type: 'BIGINT UNSIGNED', remarks: '环境部署任务关联的部署信息')
            column(name: 'command_id', type: 'BIGINT UNSIGNED', remarks: '操作ID')
            column(name: "deploy_metadata", type: "VARCHAR(2000)", remarks: '主机部署 制品库提供信息')

            column(name: 'countersigned', type: 'TINYINT UNSIGNED', remarks: '是否会签 1是会签,0 是或签')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(author: 'wanghao', id: '2020-07-02-idx-stage-record-id') {
        createIndex(indexName: "idx_stage_record_id ", tableName: "devops_cd_job_record") {
            column(name: "stage_record_id")
        }
    }

    changeSet(author: 'wanghao', id: '2020-09-14-add-column') {
        addColumn(tableName: 'devops_cd_job_record') {
            column(name: 'api_test_task_record_id', type: 'BIGINT UNSIGNED', remarks: '测试项目记录id', beforeColumn: "deploy_metadata")
        }
    }

    changeSet(author: 'scp', id: '2020-09-15-add-column') {
        addColumn(tableName: 'devops_cd_job_record') {
            column(name: 'log', type: 'text', remarks: '日志信息', beforeColumn: "countersigned")
        }
    }
    changeSet(author: 'wanghao', id: '2020-12-10-add-column') {
        addColumn(tableName: 'devops_cd_job_record') {
            column(name: 'callback_token', type: 'VARCHAR(255)', remarks: '外部卡点任务回调认证token', beforeColumn: "countersigned")
        }
    }
    changeSet(author: 'wanghao', id: '2023-03-28-drop-create-table') {
        dropTable(tableName: "devops_cd_job_record")
    }
}