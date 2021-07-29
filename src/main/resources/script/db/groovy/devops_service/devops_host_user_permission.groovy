package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_host_user_permission.groovy') {
    changeSet(author: 'lihao', id: '2021-07-29-create-table') {
        createTable(tableName: "devops_host_user_permission", remarks: '环境用户权限表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'login_name', type: 'VARCHAR(32)', remarks: '用户id')
            column(name: 'real_name', type: 'VARCHAR(32)', remarks: '真实名字', afterColumn: 'login_name')
            column(name: 'iam_user_id', type: 'BIGINT UNSIGNED', remarks: 'id', afterColumn: 'login_name')
            column(name: 'host_id', type: 'BIGINT UNSIGNED', remarks: '主机id')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
    addUniqueConstraint(tableName: 'devops_host_user_permission',
            constraintName: 'uk_iam_user_id_host_id', columnNames: 'iam_user_id,host_id')
}