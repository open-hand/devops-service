package script.db

databaseChangeLog(logicalFilePath: 'dba/devops_notification_user_rel.groovy') {
    changeSet(author: 'scp', id: '2019-05-13-create-table') {
        createTable(tableName: "devops_notification_user_rel", remarks: '通知设置') {
            column(name: 'notification_id', type: 'BIGINT UNSIGNED', remarks: '通知Id')
            column(name: 'user_id', type: 'BIGINT UNSIGNED', remarks: '用户Id')
        }
    }
}