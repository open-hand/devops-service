package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_deployment.groovy') {
    changeSet(author: 'lihao', id: '2021-06-08-create-table') {
        createTable(tableName: "devops_deployment", remarks: 'deployment资源表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(32)', remarks: 'name')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目id')
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: 'env Id')
            column(name: 'command_id', type: 'BIGINT UNSIGNED', remarks: '操作id')
            column(name: "instance_id", type: 'BIGINT UNSIGNED', remarks: '所属对象id chart产生的appServiceInstanceId/部署组产生的应用Id ')
            column(name: 'source_type', type: 'VARCHAR(32)', remarks: '来源类型 chart/工作负载/部署组')

            column(name: 'app_config', type: 'text', remarks: '应用配置')
            column(name: 'container_config', type: 'text', remarks: '容器配置')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        addUniqueConstraint(tableName: 'devops_deployment',
                constraintName: 'uk_env_id_name', columnNames: 'env_id,name')

        createIndex(indexName: "idx_env_id", tableName: "devops_deployment") {
            column(name: "env_id")
        }
    }
    changeSet(author: 'wanghao', id: '2021-08-26-add-column') {
        addColumn(tableName: 'devops_deployment') {
            column(name: 'status', type: 'VARCHAR(32)', remarks: '实例状态', afterColumn: 'source_type')
        }
    }

    changeSet(author: 'lihao', id: '2021-09-26-update-column') {
        modifyDataType(tableName: 'devops_deployment', columnName: 'name', newDataType: 'VARCHAR(64)')
    }

    changeSet(author: 'lihao',id: '2021-11-02-drop-index'){
        dropIndex(indexName: "idx_env_id", tableName: "devops_deployment")
    }
}