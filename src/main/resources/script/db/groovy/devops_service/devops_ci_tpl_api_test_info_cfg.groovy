package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_tpl_api_test_info_cfg.groovy') {
    changeSet(author: 'lihao', id: '2022-11-07-create-table-tpl-api') {
        createTable(tableName: "devops_ci_tpl_api_test_info_cfg", remarks: 'CI API测试任务配置信息模板表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'enable_warning_setting', type: 'TINYINT', remarks: '是否启用告警设置')
            column(name: 'perform_threshold', type: 'DOUBLE', remarks: '阈值')

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
}