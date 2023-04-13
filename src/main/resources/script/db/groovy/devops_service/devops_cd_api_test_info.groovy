package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_cd_api_test_info.groovy') {
    changeSet(author: 'wanghao', id: '2021-12-17-create-table') {
        createTable(tableName: "devops_cd_api_test_info", remarks: 'CD API测试任务配置信息表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'task_type', type: 'VARCHAR(32)', remarks: '任务类型：任务task 套件suite')
            column(name: 'api_test_task_id', type: 'BIGINT UNSIGNED', remarks: '测试任务id')
            column(name: 'api_test_suite_id', type: 'BIGINT UNSIGNED', remarks: '测试套件id')
            column(name: 'api_test_config_id', type: 'BIGINT UNSIGNED', remarks: '测试任务关联的任务配置id')
            column(name: 'deploy_job_name', type: 'VARCHAR(100)', remarks: '部署任务名称')
            column(name: 'enable_warning_setting', type: 'TINYINT', remarks: '是否启用告警设置')
            column(name: 'perform_threshold', type: 'DOUBLE', remarks: '阈值')
            column(name: 'notify_user_ids', type: 'VARCHAR(2048)', remarks: '通知对象集合')
            column(name: 'send_email', type: 'TINYINT', remarks: '是否发送邮件')
            column(name: 'send_site_message', type: 'TINYINT', remarks: '是否发送站内信')
            column(name: 'block_after_job', type: 'TINYINT', remarks: '是否中断后续的任务')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
    changeSet(author: 'wanghao', id: '2023-03-28-drop-create-table') {
        dropTable(tableName: "devops_cd_api_test_info")
    }
}