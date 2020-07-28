package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_cd_job.groovy') {
    changeSet(author: 'wx', id: '2020-06-30-create-table') {
        createTable(tableName: "devops_cd_job", remarks: 'devops_cd_job') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(50)', remarks: '任务job名称')
            column(name: 'pipeline_id', type: 'BIGINT UNSIGNED', remarks: '流水线id') {
                constraints(nullable: false)
            }
            column(name: 'stage_id', type: 'BIGINT UNSIGNED', remarks: '阶段id') {
                constraints(nullable: false)
            }
            column(name: 'type', type: 'VARCHAR(255)', remarks: '包含cicd所有的任务job类型') {
                constraints(nullable: false)
            }
            column(name: 'trigger_type', type: 'VARCHAR(255)', remarks: '触发方式', defaultValue: 'refs') {
                constraints(nullable: false)
            }
            column(name: 'trigger_value', type: 'VARCHAR(255)', remarks: '触发分支')

            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目ID') {
                constraints(nullable: false)
            }
            column(name: 'sequence', type: 'BIGINT UNSIGNED', remarks: '任务顺序') {
                constraints(nullable: false)
            }
            column(name: 'deploy_info_id', type: 'BIGINT UNSIGNED', remarks: '环境部署任务关联的部署信息')
            column(name: 'metadata', type: 'TEXT', remarks: 'job详细信息，定义了job执行内容')

            column(name: 'countersigned', type: 'TINYINT UNSIGNED', remarks: '是否会签 1是会签,0 是或签')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}