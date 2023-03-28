package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_cd_env_deploy_info.groovy') {
    changeSet(author: 'wanghao', id: '2020-07-08-create-table') {
        createTable(tableName: "devops_cd_env_deploy_info", remarks: 'CD环境部署任务配置表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'app_service_id', type: 'BIGINT UNSIGNED', remarks: '应用服务Id')
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境Id')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目id')
            column(name: 'value_id', type: 'BIGINT UNSIGNED', remarks: 'valueId')
            column(name: 'deploy_type', type: 'VARCHAR(100)', remarks: '部署类型：create 新建实例， update 替换实例')
            column(name: 'instance_id', type: 'BIGINT UNSIGNED', remarks: '实例ID')
            column(name: 'instance_name', type: 'VARCHAR(50)', remarks: '实例名称')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(author: 'scp', id: '2020-07-13-add-column') {
        addColumn(tableName: 'devops_cd_env_deploy_info') {
            column(name: "cd_job_id", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "jar_name", type: "VARCHAR(50)")
        }
    }
    changeSet(author: 'wanghao', id: '2020-11-06-modify-column') {
        modifyDataType(tableName: 'devops_cd_env_deploy_info', columnName: 'jar_name', newDataType: 'VARCHAR(255)')
    }

    changeSet(author: 'wanghao', id: '2020-11-25-add-column') {
        addColumn(tableName: 'devops_cd_env_deploy_info') {
            column(name: "check_env_permission_flag", type: "TINYINT UNSIGNED", defaultValue: "0", remarks: '是否校验环境权限')
        }
    }
    changeSet(author: 'wanghao', id: '2021-09-14-add-column') {
        addColumn(tableName: 'devops_cd_env_deploy_info') {
            column(name: 'app_id', type: 'BIGINT UNSIGNED', remarks: '应用id', afterColumn: "deploy_type")
            column(name: 'app_name', type: 'VARCHAR(64)', remarks: '应用名称', afterColumn: "app_id")
            column(name: 'app_code', type: 'VARCHAR(64)', remarks: '应用编码', afterColumn: "app_name")
            column(name: "skip_check_permission", type: "TINYINT UNSIGNED", defaultValue: "0", remarks: '是否校验环境权限', afterColumn: "app_code")

            column(name: 'app_config_json', type: 'text', remarks: '应用配置', afterColumn: "skip_check_permission")
            column(name: 'container_config_json', type: 'text', remarks: '容器配置', afterColumn: "app_config_json")
        }
    }
    changeSet(author: 'wanghao', id: '2023-03-28-drop-create-table') {
        dropTable(tableName: "devops_cd_env_deploy_info")
    }


}