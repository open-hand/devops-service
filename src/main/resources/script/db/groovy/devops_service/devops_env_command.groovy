package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_env_command.groovy') {
    changeSet(author: 'Younger', id: '2018-05-15-create-table') {
        createTable(tableName: "devops_env_command", remarks: '对象操作') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'object', type: 'VARCHAR(32)', remarks: '操作对象')
            column(name: 'object_id', type: 'BIGINT UNSIGNED', remarks: '操作对象ID')
            column(name: 'value_id', type: 'BIGINT UNSIGNED', remarks: '参数ID ')
            column(name: 'command_type', type: 'VARCHAR(32)', remarks: '操作类型')
            column(name: 'status', type: 'VARCHAR(32)', remarks: '操作状态')
            column(name: 'error', type: 'VARCHAR(5000)', remarks: '错误信息')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(author: 'younger', id: '2018-09-10-add-column')
            {
                addColumn(tableName: 'devops_env_command') {
                    column(name: 'sha', type: 'VARCHAR(128)', remarks: 'commit sha', afterColumn: 'status')
                }
            }


    changeSet(author: 'younger', id: '2018-10-23-add-column')
            {
                addColumn(tableName: 'devops_env_command') {
                    column(name: 'object_version_id', type: 'BIGINT UNSIGNED', remarks: 'object version id', afterColumn: 'object_id')
                }

            }

    changeSet(author: 'younger', id: '2018-10-25-update-data') {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "devops_app_instance")
            columnExists(tableName: "devops_app_instance",columnName:"app_version_id")
            columnExists(tableName: "devops_app_instance",columnName:"command_id")
            tableExists(tableName: "devops_env_command")
            columnExists(tableName: "devops_env_command",columnName:"id")
            columnExists(tableName: "devops_env_command",columnName:"object_version_id")
        }
                sql("update devops_app_instance A,devops_env_command B set B.object_version_id=A.app_version_id where A.command_id=B.id")
            }

    changeSet(author: 'scp', id: '2019-06-05-idx-object-id') {
        createIndex(indexName: "idx_object_id ", tableName: "devops_env_command") {
            column(name: "object_id")
            column(name: 'object')
        }
    }

    changeSet(author: 'scp', id: '2019-06-05-modify-index') {

        dropIndex(indexName: "idx_object_id", tableName: "devops_env_command")

        createIndex(indexName: "idx_object_object_id ", tableName: "devops_env_command") {
            column(name: "object_id")
            column(name: 'object')
        }
    }

    changeSet(author: 'lihao', id: '2023-02-28-updateDataType') {
        modifyDataType(tableName: 'devops_env_command', columnName: 'error', newDataType: 'TEXT')
    }


}