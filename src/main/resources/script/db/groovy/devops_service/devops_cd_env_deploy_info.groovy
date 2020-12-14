package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_cd_env_deploy_info.groovy') {
    changeSet(author: 'wanghao', id: '2020-07-08-create-table') {
        createTable(tableName: "devops_cd_env_deploy_info", remarks: 'value ID') {
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

}