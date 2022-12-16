package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_ci_tpl_host_deploy_info_cfg.groovy') {
    changeSet(author: 'lihao', id: '2022-11-07-create-table-tpl-host') {
        createTable(tableName: "devops_ci_tpl_host_deploy_info_cfg", remarks: 'ci主机部署任务配置模板表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }

            column(name: 'host_deploy_type', type: 'VARCHAR(20)', remarks: '主机部署类型 jar/other')
            column(name: 'pre_command', type: 'TEXT', remarks: '前置命令')
            column(name: 'run_command', type: 'TEXT', remarks: '运行命令')
            column(name: 'post_command', type: 'TEXT', remarks: '后置命令')
            column(name: "kill_command", type: "TEXT", remarks: "删除命令")
            column(name: "health_prob", type: "TEXT", remarks: "健康探针")
            column(name: "docker_command", type: "TEXT", remarks: "删除命令")
            column(name: "image_job_name", type: "VARCHAR(64)", remarks: "关联镜像构建任务名称")

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(author: 'lihao',id: '2022-11-07-add-column'){
        addColumn(tableName: 'devops_ci_tpl_host_deploy_info_cfg') {
            column(name: 'deploy_json', type: 'TEXT', remarks: '部署信息')
        }
    }
}