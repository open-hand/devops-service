package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_app_user_permission.groovy') {
    changeSet(author: 'n1ck', id: '2018-11-21-create-table') {
        createTable(tableName: "devops_app_user_permission", remarks: '应用用户权限表') {
            column(name: 'iam_user_id', type: 'BIGINT UNSIGNED', remarks: '用户id')
            column(name: 'login_name', type: 'VARCHAR(32)', remarks: '用户登陆名')
            column(name: 'real_name', type: 'VARCHAR(32)', remarks: '用户真实名')
            column(name: 'app_id', type: 'BIGINT UNSIGNED', remarks: '应用id')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")

        }

        addUniqueConstraint(tableName: 'devops_app_user_permission',
                constraintName: 'uk_iam_user_id_app_id', columnNames: 'iam_user_id,app_id')

        createIndex(indexName: "idx_app_id", tableName: "devops_app_user_permission") {
            column(name: "app_id")
        }
    }

    changeSet(author: 'younger', id: '2018-11-27-rename-table') {
        dropColumn(columnName: "login_name", tableName: "devops_app_user_permission")
        dropColumn(columnName: "real_name", tableName: "devops_app_user_permission")
        renameTable(newTableName: 'devops_app_user_rel', oldTableName: 'devops_app_user_permission')
    }

    changeSet(author: 'scp', id: '2019-07-29-rename-column') {
        renameColumn(columnDataType: 'BIGINT UNSIGNED', newColumnName: 'app_service_id', oldColumnName: 'app_id', tableName: 'devops_app_user_rel')
    }

    changeSet(author: 'sheep', id: '2019-8-05-rename-table') {
        renameTable(newTableName: 'devops_app_service_user_rel', oldTableName: 'devops_app_user_rel')
    }

    changeSet(author: 'zmf', id: '2020-05-13-devops_app_service_user_rel-add-pk') {
        addColumn(tableName: 'devops_app_service_user_rel') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true, beforeColumn: "iam_user_id") {
                constraints(primaryKey: true)
            }
        }
    }

    changeSet(author: 'zmf', id: '2020-05-13-devops_app_service_user_rel-add-uk', failOnError: false) {
        addUniqueConstraint(tableName: 'devops_app_service_user_rel',
                constraintName: 'uk_devops_app_service_user_rel_app_service_id_iam_user_id', columnNames: 'app_service_id,iam_user_id')
    }

    changeSet(author: 'lihao',id: '2021-11-02-drop-index'){
        dropIndex(indexName: "idx_app_id", tableName: "devops_app_service_user_rel")
    }
}