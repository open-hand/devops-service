package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_env_user_permission.groovy') {
    changeSet(author: 'n1ck', id: '2018-10-25-create-table') {
        createTable(tableName: "devops_env_user_permission", remarks: '环境用户权限表') {

            column(name: 'login_name', type: 'VARCHAR(32)', remarks: '用户id')
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境id')
            column(name: 'isPermitted', type: 'TINYINT UNSIGNED', remarks: '是否有权限', defaultValue: '0')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

    changeSet(author: 'n1ck', id: '2018-10-26-add-real_name') {
        addColumn(tableName: 'devops_env_user_permission') {
            column(name: 'real_name', type: 'VARCHAR(32)', remarks: '真实名字', afterColumn: 'login_name')
        }
    }
}