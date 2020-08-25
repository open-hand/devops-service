package script.db.groovy

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

}