package script.db


databaseChangeLog(logicalFilePath: 'db/devops_env_commit.groovy') {
    changeSet(author: 'Younger', id: '2018-08-01-create-table') {
        createTable(tableName: "devops_env_commit", remarks: '环境commit') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'env_id', type: 'BIGINT UNSIGNED', remarks: '环境 ID ')
            column(name: 'commit_user', type: 'BIGINT UNSIGNED', remarks: '提交人')
            column(name: 'commit_sha', type: 'VARCHAR(100)', remarks: '提交信息')
            column(name: 'commit_date', type: 'DATETIME', remarks: '提交时间')

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }

}