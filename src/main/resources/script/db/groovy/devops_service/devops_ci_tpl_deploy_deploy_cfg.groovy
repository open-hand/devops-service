package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_tpl_deploy_deploy_cfg.groovy') {
    changeSet(author: 'wanghao', id: '2020-07-08-create-table') {
        createTable(tableName: "devops_ci_tpl_deploy_deploy_cfg", remarks: 'CI deployment部署任务配置表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境Id,devops_env.id')
            column(name: 'deploy_type', type: 'VARCHAR(100)', remarks: '部署类型：create 新建实例， update 替换实例')
            column(name: "skip_check_permission", type: "TINYINT UNSIGNED", defaultValue: "0", remarks: '是否校验环境权限')
            column(name: 'app_name', type: 'VARCHAR(64)', remarks: '应用名称,devops_deploy_app_center_env.name')
            column(name: 'app_code', type: 'VARCHAR(64)', remarks: '应用编码,devops_deploy_app_center_env.code')
            column(name: 'app_config_json', type: 'text', remarks: '应用配置')
            column(name: 'container_config_json', type: 'text', remarks: '容器配置')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

}