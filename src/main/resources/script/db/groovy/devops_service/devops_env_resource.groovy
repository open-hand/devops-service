package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_env_resource.groovy') {
    changeSet(author: 'Younger', id: '2018-04-24-create-table') {
        createTable(tableName: "devops_env_resource", remarks: '环境资源表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'app_instance_id', type: 'BIGINT UNSIGNED', remarks: '部署实例 ID')
            column(name: 'message_id', type: 'BIGINT UNSIGNED', remarks: '资源信息id')
            column(name: 'kind', type: 'VARCHAR(32)', remarks: '资源类型')
            column(name: 'name', type: 'VARCHAR(64)', remarks: '资源名')
            column(name: 'weight ', type: 'BIGINT UNSIGNED', remarks: 'hook执行顺序')
            column(name: 'reversion ', type: 'BIGINT UNSIGNED', remarks: '判断对象是否更新')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(id: '2018-10-08-rename-column', author: 'younger') {
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'resource_detail_id', oldColumnName: 'message_id', remarks: '资源信息', tableName: 'devops_env_resource')
    }

    changeSet(author: 'younger', id: '2018-11-26-add-column') {
        addColumn(tableName: 'devops_env_resource') {
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: 'env', afterColumn: 'app_instance_id')
            column(name: 'command_id', type: 'BIGINT UNSIGNED', remarks: 'command', afterColumn: 'env_id')
        }
    }

    changeSet(author: 'younger', id: '2019-05-27-add-index') {
        createIndex(indexName: "idx_appinstanceid ", tableName: "devops_env_resource") {
            column(name: "app_instance_id")
        }
    }

    changeSet(author: 'sheep', id: '2019-08-05-rename-column') {
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'instance_id', oldColumnName: 'app_instance_id', tableName: 'devops_env_resource')
    }

    changeSet(author: 'zmf', id: '2020-05-13-add-env-resource-uk', failOnError: false) {
        addUniqueConstraint(tableName: 'devops_env_resource',
                constraintName: 'uk_devops_env_resource_env_id_kind_name', columnNames: 'env_id,kind,name')
    }

    changeSet(author: 'younger', id: '2020-06-29-devops_env_resource-add-index') {
        createIndex(indexName: "idx_devops_env_resource_name_kind_creationdate", tableName: "devops_env_resource") {
            column(name: "name")
            column(name: "kind")
            column(name: "creation_date")
        }
    }

    changeSet(author: 'wanghao', id: '2022-10-30-add-index') {
        createIndex(indexName: "idx-resource-detail-id", tableName: "devops_env_resource") {
            column(name: "resource_detail_id")
        }
    }

    changeSet(author: 'wanghao', id: '2023-01-03-add-index') {
        createIndex(indexName: "devops_env_resource_u1 ", tableName: "devops_env_resource") {
            column(name: "env_id")
            column(name: "kind")
            column(name: "name")
        }
    }
}