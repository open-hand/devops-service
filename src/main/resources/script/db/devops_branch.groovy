package script.db


databaseChangeLog(logicalFilePath: 'dba/devops_branch.groovy') {
    changeSet(author: 'Younger', id: '2018-07-01-create-table') {
        createTable(tableName: "devops_branch", remarks: 'git分支') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'app_id', type: 'BIGINT UNSIGNED', remarks: '应用Id')
            column(name: 'user_id', type: 'BIGINT UNSIGNED', remarks: '用户Id')
            column(name: 'issue_id', type: 'BIGINT UNSIGNED', remarks: 'issueId')
            column(name: 'branch_name', type: 'VARCHAR(64)', remarks: '分支名')
            column(name: 'origin_branch', type: 'VARCHAR(64)', remarks: '来源分支')
            column(name: 'last_commit_date', type: 'DATETIME', remarks: '最后提交时间')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}