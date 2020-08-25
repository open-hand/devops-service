package script.db.groovy

databaseChangeLog(logicalFilePath: 'dba/devops_notification_user_rel.groovy') {
    changeSet(author: 'scp', id: '2019-05-13-create-table') {
        createTable(tableName: "devops_notification_user_rel", remarks: '通知设置') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'notification_id', type: 'BIGINT UNSIGNED', remarks: '通知Id')
            column(name: 'user_id', type: 'BIGINT UNSIGNED', remarks: '用户Id')
        }
    }

    changeSet(author: 'zmf', id: '2020-05-13-devops_notification_user_rel-add-uk', failOnError: false) {
        addUniqueConstraint(tableName: 'devops_notification_user_rel',
                constraintName: 'uk_devops_notification_user_rel_notification_id_user_id', columnNames: 'notification_id,user_id')
    }
}