package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_deploy_record.groovy') {
    changeSet(author: 'Sheep', id: '2019-07-29-create-table') {
        createTable(tableName: "devops_deploy_record", remarks: '部署记录表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，环境ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目ID')
            column(name: 'env', type: 'VARCHAR(32)', remarks: '部署关联环境')
            column(name: 'deploy_id', type: 'BIGINT UNSIGNED', remarks: '部署ID')
            column(name: 'deploy_type', type: 'VARCHAR(32)', remarks: '部署类型')
            column(name: "deploy_time", type: "DATETIME")
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(author: 'zmf', id: '2019-09-28-add-unique-index') {
        createIndex(tableName: 'devops_deploy_record', unique: 'true', indexName: 'dp_record_deploy_id_type_unique_idx') {
            column(name: 'deploy_id', type: 'BIGINT UNSIGNED')
            column(name: 'deploy_type', type: 'VARCHAR(32)')
        }
    }

    changeSet(author: 'lihao', id: '2020-07-19-add-index') {
        createIndex(tableName: 'devops_deploy_record', indexName: 'dp_record_project_id_type__idx') {
            column(name: 'project_id')
            column(name: 'deploy_type')
        }
    }

    changeSet(author: 'wanghao', id: '2020-10-12-drop-record') {
        sql("""
            DELETE FROM devops_deploy_record WHERE deploy_type = 'batch' or deploy_type = 'auto';
        """)
    }
    changeSet(author: 'wanghao', id: '2020-10-20-add-column') {
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'deploy_payload_id', oldColumnName: 'env', tableName: 'devops_deploy_record')
        addColumn(tableName: 'devops_deploy_record') {
            column(name: 'deploy_mode', type: 'varchar(100)', remarks: '部署模式， env 环境部署，host主机部署', afterColumn: 'deploy_type')
            column(name: 'deploy_payload_name', type: 'varchar(255)', remarks: '部署载体name 主机名/环境名', afterColumn: 'deploy_mode')
            column(name: 'deploy_status', type: 'varchar(255)', remarks: '执行结果, 环境部署的结果根据deploy_id从env_command表获取', afterColumn: 'deploy_payload_name')
            column(name: 'deploy_object_type', type: 'varchar(255)', remarks: '部署对象类型 app 应用服务，jar ,image', afterColumn: 'deploy_result')
            column(name: 'deploy_object_name', type: 'varchar(255)', remarks: '部署对象名', afterColumn: 'deploy_object_type')
            column(name: 'deploy_object_version', type: 'varchar(255)', remarks: '部署对象版本', afterColumn: 'deploy_object_name')
            column(name: 'instance_code', type: 'varchar(255)', remarks: '实例code', afterColumn: 'deploy_object_version')
        }
    }

}