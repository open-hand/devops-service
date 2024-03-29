package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_stateful_set.groovy') {
    changeSet(author: 'lihao', id: '2021-06-08-create-table') {
        createTable(tableName: "devops_stateful_set", remarks: 'statefulset资源表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(32)', remarks: 'name')
            column(name: 'project_id', type: 'BIGINT UNSIGNED', remarks: '项目id')
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: 'env Id')
            column(name: 'command_id', type: 'BIGINT UNSIGNED', remarks: '操作id')
            column(name: "instance_id", type: 'BIGINT UNSIGNED', remarks: '所属实例id')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }

        addUniqueConstraint(tableName: 'devops_stateful_set',
                constraintName: 'uk_env_id_name', columnNames: 'env_id,name')

        createIndex(indexName: "idx_env_id", tableName: "devops_stateful_set") {
            column(name: "env_id")
        }
    }

    changeSet(author: 'lihao', id: '2021-09-26-update-column') {
        modifyDataType(tableName: 'devops_stateful_set', columnName: 'name', newDataType: 'VARCHAR(64)')
    }

    changeSet(author: 'lihao',id: '2021-11-02-drop-index'){
        dropIndex(indexName: "idx_env_id", tableName: "devops_stateful_set")
    }

}