package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_cd_job.groovy') {
    changeSet(author: 'wx', id: '2020-06-30-create-table') {
        createTable(tableName: "devops_cd_job", remarks: 'devops_cd_job') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(50)', remarks: '任务job名称')
            column(name: 'cicd_pipeline_id', type: 'BIGINT UNSIGNED', remarks: '流水线id')
            column(name: 'cicd_stage_id', type: 'BIGINT UNSIGNED', remarks: '阶段id')
            column(name: 'type', type: 'VARCHAR(255)', remarks: '包含cicd所有的任务job类型')
            column(name: 'trigger_type', type: 'VARCHAR(255)', remarks: '触发方式', defaultValue: 'refs')
            column(name: 'trigger_refs', type: 'VARCHAR(255)', remarks: '触发分支')
            column(name: 'image', type: 'VARCHAR(280)', remarks: 'job的镜像地址') {
                constraints(nullable: true)
            }
            column(name: 'is_to_upload', type: 'TINYINT(1) UNSIGNED', defaultValue: "0", remarks: '是否上传共享目录的内容, 默认为false') {
                constraints(nullable: false)
            }
            column(name: 'is_to_download', type: 'TINYINT(1) UNSIGNED', defaultValue: "0", remarks: '是否下载共享目录的内容,默认为false') {
                constraints(nullable: false)
            }
            column(name: 'metadata', type: 'VARCHAR(2000)', remarks: 'job详细信息，定义了job执行内容')

            column(name: 'app_service_deploy_id', type: 'BIGINT UNSIGNED', remarks: '应用部署Id')
            column(name: 'is_countersigned', type: 'TINYINT UNSIGNED', remarks: '是否会签')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目ID')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}