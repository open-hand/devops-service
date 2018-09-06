package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_user.groovy') {
    changeSet(author: 'Runge', id: '2018-03-27-create-table') {
        createTable(tableName: "devops_user", remarks: 'DevOps 用户表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，Iam用户ID') {
                constraints(primaryKey: true)
            }
            column(name: 'gitlab_user_id', type: 'BIGINT UNSIGNED', remarks: 'Gitlab用户ID') {
                constraints(unique: true)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}