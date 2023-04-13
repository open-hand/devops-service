package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_cd_host_deploy_info.groovy') {
    changeSet(author: 'wanghao', id: '2021-09-14-create-table') {
        createTable(tableName: "devops_cd_host_deploy_info", remarks: 'CD主机部署任务配置表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'host_id', type: 'BIGINT UNSIGNED', remarks: '主机Id')
            column(name: 'deploy_type', type: 'VARCHAR(100)', remarks: '部署类型：create 新建实例， update 替换实例')
            column(name: 'app_id', type: 'BIGINT UNSIGNED', remarks: '应用id')
            column(name: 'app_name', type: 'VARCHAR(64)', remarks: '应用名称')
            column(name: 'app_code', type: 'VARCHAR(64)', remarks: '应用编码')
            column(name: 'host_deploy_type', type: 'VARCHAR(64)', remarks: '主机部署类型 jar/other')
            column(name: 'jar_deploy_json', type: 'VARCHAR(2048)', remarks: 'jar部署配置json')

            column(name: 'pre_command', type: 'TEXT', remarks: '前置命令')
            column(name: 'run_command', type: 'TEXT', remarks: '运行命令')
            column(name: 'post_command', type: 'TEXT', remarks: '后置命令')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(author: 'lihao', id: '2022-02-15-add-column') {
        addColumn(tableName: 'devops_cd_host_deploy_info') {
            column(name: "kill_command", type: "TEXT", remarks: "删除命令", afterColumn: "post_command")
            column(name: "health_prob", type: "TEXT", remarks: "健康探针", afterColumn: "kill_command")
        }
    }
    changeSet(author: 'wx', id: '2022-02-24-update-column') {
        addColumn(tableName: 'devops_cd_host_deploy_info') {
            column(name: "docker_command", type: "TEXT", remarks: "删除命令", afterColumn: "post_command")
        }
        sql("""
              ALTER TABLE devops_cd_host_deploy_info CHANGE jar_deploy_json  deploy_json VARCHAR(2048)
        """)
    }
    changeSet(author: 'wanghao', id: '2022-04-08-add-column') {
        addColumn(tableName: 'devops_cd_host_deploy_info') {
            column(name: "image_job_name", type: "VARCHAR(64)", remarks: "关联镜像构建任务名称", afterColumn: "post_command")
        }
    }
    changeSet(author: 'wanghao', id: '2023-03-28-drop-create-table') {
        dropTable(tableName: "devops_cd_host_deploy_info")
    }

}