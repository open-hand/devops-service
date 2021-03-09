package script.db.groovy.devops_service

databaseChangeLog(logicalFilePath: 'dba/devops_user_sync_record.groovy') {
    changeSet(author: 'zmf', id: '2021-01-21-create-table-user-sync-record') {
        createTable(tableName: "devops_user_sync_record", remarks: 'DevOps用户同步记录表') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，Iam用户ID') {
                constraints(primaryKey: true)
            }


            column(name: 'type', type: 'VARCHAR(30)', remarks: "记录的类型")
            column(name: 'status', type: 'VARCHAR(30)', remarks: "记录的状态")
            column(name: 'start_time', type: 'DATETIME', remarks: '开始时间')
            column(name: 'end_time', type: 'DATETIME', remarks: '结束时间')
            column(name: 'success_count', type: 'BIGINT UNSIGNED', remarks: '同步成功用户数量', defaultValue: 0)
            column(name: 'fail_count', type: 'BIGINT UNSIGNED', remarks: '同步失败用户数量', defaultValue: 0)

            column(name: 'error_user_result_url', type: 'VARCHAR(1000)', remarks: '同步失败用户的失败详情url / 可为空') {
                constraints(nullable: true)
            }

            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}